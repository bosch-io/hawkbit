/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.model.MetaData;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.providers.DsMetaDataDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractMetaDataWindowLayout;
import org.eclipse.hawkbit.ui.common.detailslayout.AddMetaDataWindowController;
import org.eclipse.hawkbit.ui.common.detailslayout.MetaDataAddUpdateWindowLayout;
import org.eclipse.hawkbit.ui.common.detailslayout.MetaDataWindowGrid;
import org.eclipse.hawkbit.ui.common.detailslayout.UpdateMetaDataWindowController;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Class for metadata add/update window layout.
 */
public class DsMetaDataWindowLayout extends AbstractMetaDataWindowLayout<Long> {
    private static final long serialVersionUID = 1L;

    private final UINotification uiNotification;
    private final transient DistributionSetManagement dsManagement;
    private final transient EntityFactory entityFactory;

    private final MetaDataWindowGrid<Long> dsMetaDataWindowGrid;

    private final transient MetaDataAddUpdateWindowLayout metaDataAddUpdateWindowLayout;
    private final transient AddMetaDataWindowController addDsMetaDataWindowController;
    private final transient UpdateMetaDataWindowController updateDsMetaDataWindowController;

    /**
     * Constructor for AbstractTagWindowLayout
     * 
     * @param i18n
     *            I18N
     */
    public DsMetaDataWindowLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permChecker, final UINotification uiNotification,
            final EntityFactory entityFactory, final DistributionSetManagement dsManagement) {
        super(i18n, eventBus, permChecker);

        this.uiNotification = uiNotification;
        this.dsManagement = dsManagement;
        this.entityFactory = entityFactory;

        this.dsMetaDataWindowGrid = new MetaDataWindowGrid<>(i18n, eventBus, permChecker, uiNotification,
                new DsMetaDataDataProvider(dsManagement), this::deleteMetaData);

        this.metaDataAddUpdateWindowLayout = new MetaDataAddUpdateWindowLayout(i18n);
        this.addDsMetaDataWindowController = new AddMetaDataWindowController(i18n, uiNotification,
                metaDataAddUpdateWindowLayout, this::createMetaData, this::onMetaDataModified, this::isDuplicate);
        this.updateDsMetaDataWindowController = new UpdateMetaDataWindowController(i18n, uiNotification,
                metaDataAddUpdateWindowLayout, this::updateMetaData, this::onMetaDataModified);

        buildLayout();
        addGridSelectionListener();
    }

    private void deleteMetaData(final Collection<ProxyMetaData> metaDataToDelete) {
        if (masterEntityFilter != null && !CollectionUtils.isEmpty(metaDataToDelete)) {
            // as of now we only allow deletion of single metadata entry
            final String metaDataKey = metaDataToDelete.iterator().next().getKey();
            dsManagement.deleteMetaData(masterEntityFilter, metaDataKey);

            dsMetaDataWindowGrid.refreshContainer();

            publishEntityModifiedEvent();
        } else {
            // TODO: use i18n
            uiNotification.displayValidationError(
                    "No distribution set is currently selected or metadata to delete is missing");
        }
    }

    private MetaData createMetaData(final ProxyMetaData entity) {
        return dsManagement
                .createMetaData(masterEntityFilter,
                        Collections.singletonList(entityFactory.generateDsMetadata(entity.getKey(), entity.getValue())))
                .get(0);
    }

    private boolean isDuplicate(final String metaDataKey) {
        return dsManagement.getMetaDataByDistributionSetId(masterEntityFilter, metaDataKey).isPresent();
    }

    private MetaData updateMetaData(final ProxyMetaData entity) {
        return dsManagement.updateMetaData(masterEntityFilter,
                entityFactory.generateDsMetadata(entity.getKey(), entity.getValue()));
    }

    @Override
    protected MetaDataWindowGrid<Long> getMetaDataWindowGrid() {
        return dsMetaDataWindowGrid;
    }

    @Override
    public AddMetaDataWindowController getAddMetaDataWindowController() {
        return addDsMetaDataWindowController;
    }

    @Override
    public UpdateMetaDataWindowController getUpdateMetaDataWindowController() {
        return updateDsMetaDataWindowController;
    }

    @Override
    public MetaDataAddUpdateWindowLayout getMetaDataAddUpdateWindowLayout() {
        return metaDataAddUpdateWindowLayout;
    }

    @Override
    protected void publishEntityModifiedEvent() {
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                EntityModifiedEventType.ENTITY_UPDATED, ProxyDistributionSet.class, masterEntityFilter));
    }
}
