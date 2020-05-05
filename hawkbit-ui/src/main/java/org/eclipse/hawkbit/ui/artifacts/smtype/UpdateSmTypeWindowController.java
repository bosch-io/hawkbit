/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtype;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.builder.SoftwareModuleTypeUpdate;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.EntityReadOnlyException;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowController;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType.SmTypeAssign;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

public class UpdateSmTypeWindowController extends AbstractEntityWindowController<ProxyType, ProxyType> {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final SoftwareModuleTypeManagement smTypeManagement;

    private final SmTypeWindowLayout layout;

    private String nameBeforeEdit;
    private String keyBeforeEdit;

    public UpdateSmTypeWindowController(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification,
            final SoftwareModuleTypeManagement smTypeManagement, final SmTypeWindowLayout layout) {
        this.i18n = i18n;
        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;

        this.smTypeManagement = smTypeManagement;

        this.layout = layout;
    }

    @Override
    public AbstractEntityWindowLayout<ProxyType> getLayout() {
        return layout;
    }

    @Override
    protected ProxyType buildEntityFromProxy(final ProxyType proxyEntity) {
        final ProxyType smType = new ProxyType();

        smType.setId(proxyEntity.getId());
        smType.setName(proxyEntity.getName());
        smType.setDescription(proxyEntity.getDescription());
        smType.setColour(StringUtils.hasText(proxyEntity.getColour()) ? proxyEntity.getColour() : "#2c9720");
        smType.setKey(proxyEntity.getKey());
        smType.setSmTypeAssign(getSmTypeAssignById(proxyEntity.getId()));

        nameBeforeEdit = proxyEntity.getName();
        keyBeforeEdit = proxyEntity.getKey();

        return smType;
    }

    private SmTypeAssign getSmTypeAssignById(final Long id) {
        return smTypeManagement.get(id)
                .map(smType -> smType.getMaxAssignments() == 1 ? SmTypeAssign.SINGLE : SmTypeAssign.MULTI)
                .orElse(SmTypeAssign.SINGLE);
    }

    @Override
    protected void adaptLayout() {
        layout.disableTagName();
        layout.disableTypeKey();
        layout.disableTypeAssignOptionGroup();
    }

    @Override
    protected void persistEntity(final ProxyType entity) {
        final SoftwareModuleTypeUpdate smTypeUpdate = entityFactory.softwareModuleType().update(entity.getId())
                .description(entity.getDescription()).colour(entity.getColour());

        final SoftwareModuleType updatedSmType;
        try {
            updatedSmType = smTypeManagement.update(smTypeUpdate);
        } catch (final EntityNotFoundException | EntityReadOnlyException e) {
            uiNotification.displayWarning(i18n.getMessage("message.deleted.or.notAllowed", "Type", entity.getName()));
            return;
        }

        uiNotification.displaySuccess(i18n.getMessage("message.update.success", updatedSmType.getName()));
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new EntityModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, ProxySoftwareModule.class,
                        ProxyType.class, updatedSmType.getId()));
    }

    @Override
    protected boolean isEntityValid(final ProxyType entity) {
        if (!StringUtils.hasText(entity.getName()) || !StringUtils.hasText(entity.getKey())) {
            uiNotification.displayValidationError(i18n.getMessage("message.error.missing.typenameorkey"));
            return false;
        }

        final String trimmedName = StringUtils.trimWhitespace(entity.getName());
        final String trimmedKey = StringUtils.trimWhitespace(entity.getKey());
        if (!nameBeforeEdit.equals(trimmedName) && smTypeManagement.getByName(trimmedName).isPresent()) {
            // TODO: is the notification right here?
            uiNotification.displayValidationError(i18n.getMessage("message.tag.duplicate.check", trimmedName));
            return false;
        }
        if (!keyBeforeEdit.equals(trimmedKey) && smTypeManagement.getByKey(trimmedKey).isPresent()) {
            // TODO: is the notification right here?
            uiNotification
                    .displayValidationError(i18n.getMessage("message.type.key.swmodule.duplicate.check", trimmedKey));
            return false;
        }

        return true;
    }
}
