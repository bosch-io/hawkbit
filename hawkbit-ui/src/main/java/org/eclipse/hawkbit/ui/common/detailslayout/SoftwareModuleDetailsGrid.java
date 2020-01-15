/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModuleDetails;
import org.eclipse.hawkbit.ui.common.event.DsModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Software module details table.
 * 
 */
public class SoftwareModuleDetailsGrid extends Grid<ProxySoftwareModuleDetails> {
    private static final long serialVersionUID = 1L;

    private static final String SOFT_TYPE_NAME_ID = "typeName";
    private static final String SOFT_MODULES_ID = "softwareModules";
    private static final String SOFT_TYPE_MANDATORY_ID = "mandatory";

    private final VaadinMessageSource i18n;
    private final transient UIEventBus eventBus;
    private final UINotification uiNotification;
    private final SpPermissionChecker permissionChecker;

    private final transient DistributionSetManagement distributionSetManagement;
    private final transient SoftwareModuleManagement smManagement;
    private final transient DistributionSetTypeManagement dsTypeManagement;

    private final boolean isUnassignSmAllowed;

    private ProxyDistributionSet masterEntity;
    private final Map<Long, Boolean> typeIdIsRendered;

    /**
     * Initialize software module table- to be displayed in details layout.
     * 
     * @param i18n
     *            I18N
     * @param eventBus
     *            SessionEventBus
     * @param uiNotification
     *            UINotification for displaying error and success notifications
     * @param permissionChecker
     *            SpPermissionChecker
     * @param distributionSetManagement
     *            DistributionSetManagement
     * @param smManagement
     *            SoftwareModuleManagement
     * @param dsTypeManagement
     *            DistributionSetTypeManagement
     * @param isUnassignSmAllowed
     *            boolean flag to check for unassign functionality allowed for
     *            the view.
     */
    public SoftwareModuleDetailsGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final UINotification uiNotification, final SpPermissionChecker permissionChecker,
            final DistributionSetManagement distributionSetManagement, final SoftwareModuleManagement smManagement,
            final DistributionSetTypeManagement dsTypeManagement, final boolean isUnassignSmAllowed) {
        this.i18n = i18n;
        this.uiNotification = uiNotification;
        this.eventBus = eventBus;
        this.permissionChecker = permissionChecker;

        this.distributionSetManagement = distributionSetManagement;
        this.smManagement = smManagement;
        this.dsTypeManagement = dsTypeManagement;

        this.isUnassignSmAllowed = isUnassignSmAllowed;

        this.typeIdIsRendered = new HashMap<>();

        init();
        setVisible(false);
    }

    private void init() {
        setSizeFull();
        setHeightMode(HeightMode.UNDEFINED);

        addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        // addStyleName(SPUIStyleDefinitions.SW_MODULE_TABLE);

        setSelectionMode(SelectionMode.NONE);

        addColumns();
    }

    private void addColumns() {
        addComponentColumn(this::buildIsMandatoryLabel).setId(SOFT_TYPE_MANDATORY_ID);

        addColumn(this::buildTypeName).setId(SOFT_TYPE_NAME_ID).setCaption(i18n.getMessage("header.caption.typename"));

        addComponentColumn(this::buildSoftwareModulesLayout).setId(SOFT_MODULES_ID)
                .setCaption(i18n.getMessage("header.caption.softwaremodule"));
    }

    private Label buildIsMandatoryLabel(final ProxySoftwareModuleDetails softwareModuleDetails) {
        final Label isMandatoryLabel = new Label("");

        isMandatoryLabel.setSizeFull();
        isMandatoryLabel.addStyleName(SPUIDefinitions.TEXT_STYLE);
        isMandatoryLabel.addStyleName("label-style");

        if (softwareModuleDetails.isMandatory() && !isTypeAlreadyAdded(softwareModuleDetails.getTypeId())) {
            isMandatoryLabel.setValue("*");
            isMandatoryLabel.setStyleName(SPUIStyleDefinitions.SP_TEXTFIELD_ERROR);
        }

        return isMandatoryLabel;
    }

    // workaround for vaadin 8 grid dynamic row height bug:
    // https://github.com/vaadin/framework/issues/9355
    private boolean isTypeAlreadyAdded(final Long typeId) {
        return typeIdIsRendered.getOrDefault(typeId, false);
    }

    private String buildTypeName(final ProxySoftwareModuleDetails softwareModuleDetails) {
        if (isTypeAlreadyAdded(softwareModuleDetails.getTypeId())) {
            return "";
        }

        return softwareModuleDetails.getTypeName();
    }

    private HorizontalLayout buildSoftwareModulesLayout(final ProxySoftwareModuleDetails softwareModuleDetails) {
        if (!isTypeAlreadyAdded(softwareModuleDetails.getTypeId())) {
            typeIdIsRendered.put(softwareModuleDetails.getTypeId(), true);
        }

        final Long smId = softwareModuleDetails.getSmId();
        final String smNameVersion = softwareModuleDetails.getSmNameAndVersion();

        final HorizontalLayout smLabelWithUnassignButtonLayout = new HorizontalLayout();
        smLabelWithUnassignButtonLayout.setSpacing(false);
        smLabelWithUnassignButtonLayout.setMargin(false);
        smLabelWithUnassignButtonLayout.setSizeFull();

        if (smId != null && !StringUtils.isEmpty(smNameVersion)) {
            smLabelWithUnassignButtonLayout.addComponent(buildSmLabel(smId, smNameVersion));

            if (isUnassignSmAllowed && permissionChecker.hasUpdateRepositoryPermission()) {
                smLabelWithUnassignButtonLayout.addComponent(buildSmUnassignButton(smId, smNameVersion));

            }
        }

        return smLabelWithUnassignButtonLayout;
    }

    private Label buildSmLabel(final Long smId, final String smNameWithVersion) {
        final Label smLabel = new Label(smNameWithVersion);

        smLabel.setId("sm-label-" + smId);
        smLabel.setSizeFull();
        smLabel.addStyleName(SPUIDefinitions.TEXT_STYLE);
        smLabel.addStyleName("label-style");

        return smLabel;
    }

    private Button buildSmUnassignButton(final Long smId, final String smNameAndVersion) {
        final Button unassignSoftwareModuleButton = new Button(VaadinIcons.CLOSE_SMALL);

        unassignSoftwareModuleButton.setId("sm-unassign-button-" + smId);
        unassignSoftwareModuleButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        unassignSoftwareModuleButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        unassignSoftwareModuleButton.addStyleName("button-no-border");

        unassignSoftwareModuleButton.addClickListener(event -> unassignSoftwareModule(smId, smNameAndVersion));

        return unassignSoftwareModuleButton;
    }

    private void unassignSoftwareModule(final Long smId, final String smNameAndVersion) {
        if (masterEntity == null) {
            // TODO: use i18n
            uiNotification.displayValidationError("no distribution set selected");
            return;
        }

        final Long dsId = masterEntity.getId();

        if (distributionSetManagement.isInUse(dsId)) {
            uiNotification.displayValidationError(i18n.getMessage("message.error.notification.ds.target.assigned",
                    masterEntity.getName(), masterEntity.getVersion()));
        } else {
            distributionSetManagement.unassignSoftwareModule(dsId, smId);

            eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                    new DsModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, dsId));
            uiNotification.displaySuccess(i18n.getMessage("message.sw.unassigned", smNameAndVersion));
        }
    }

    public void updateMasterEntityFilter(final ProxyDistributionSet masterEntityFilter) {
        masterEntity = masterEntityFilter;
        typeIdIsRendered.clear();

        if (masterEntity == null) {
            setItems(Collections.emptyList());
            setVisible(false);

            return;
        }

        final Optional<DistributionSetType> dsType = dsTypeManagement.get(masterEntity.getTypeId());

        final List<ProxySoftwareModuleDetails> items = new ArrayList<>();

        // TODO: try to optimize
        dsType.ifPresent(type -> {
            final Collection<SoftwareModule> softwareModules = getSoftwareModulesByDsId(masterEntity.getId());

            for (final SoftwareModuleType mandatoryType : type.getMandatoryModuleTypes()) {
                items.addAll(getSmDetailsByType(softwareModules, mandatoryType, true));

                typeIdIsRendered.put(mandatoryType.getId(), false);
            }

            for (final SoftwareModuleType optionalType : type.getOptionalModuleTypes()) {
                items.addAll(getSmDetailsByType(softwareModules, optionalType, false));

                typeIdIsRendered.put(optionalType.getId(), false);
            }
        });

        setItems(items);
        setVisible(true);
    }

    private Collection<SoftwareModule> getSoftwareModulesByDsId(final Long dsId) {
        Pageable query = PageRequest.of(0, SPUIDefinitions.PAGE_SIZE);
        Page<SoftwareModule> smPage;
        final Collection<SoftwareModule> softwareModules = new ArrayList<>();

        do {
            smPage = smManagement.findByAssignedTo(query, dsId);
            softwareModules.addAll(smPage.getContent());
        } while ((query = smPage.nextPageable()) != Pageable.unpaged());

        return softwareModules;
    }

    private List<ProxySoftwareModuleDetails> getSmDetailsByType(final Collection<SoftwareModule> softwareModules,
            final SoftwareModuleType type, final boolean isMandatory) {
        final List<ProxySoftwareModuleDetails> smDetails = softwareModules.stream()
                .filter(sm -> sm.getType().getId().equals(type.getId()))
                .map(sm -> new ProxySoftwareModuleDetails(isMandatory, type.getId(), type.getName(), sm.getId(),
                        HawkbitCommonUtil.concatStrings(":", sm.getName(), sm.getVersion())))
                .collect(Collectors.toList());

        if (smDetails.isEmpty()) {
            return Collections
                    .singletonList(new ProxySoftwareModuleDetails(isMandatory, type.getId(), type.getName(), null, ""));
        }

        return smDetails;
    }
}