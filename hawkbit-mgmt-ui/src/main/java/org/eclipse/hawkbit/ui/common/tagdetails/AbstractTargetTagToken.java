/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.tagdetails;

import org.eclipse.hawkbit.eventbus.event.TargetTagCreatedBulkEvent;
import org.eclipse.hawkbit.eventbus.event.TargetTagDeletedEvent;
import org.eclipse.hawkbit.repository.TagManagement;
import org.eclipse.hawkbit.repository.model.BaseEntity;
import org.eclipse.hawkbit.repository.model.TargetTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

/**
 * Abstract class for target tag token layout.
 */
public abstract class AbstractTargetTagToken<T extends BaseEntity> extends AbstractTagToken<T> {

    private static final long serialVersionUID = 7772876588903171201L;

    @Autowired
    protected transient TagManagement tagManagement;

    @EventBusListenerMethod(scope = EventScope.SESSION)
    void onEventTargetTagCreated(final TargetTagCreatedBulkEvent event) {
        for (final TargetTag tag : event.getEntities()) {
            setContainerPropertValues(tag.getId(), tag.getName(), tag.getColour());
        }
    }

    @EventBusListenerMethod(scope = EventScope.SESSION)
    void onTargetDeletedEvent(final TargetTagDeletedEvent event) {
        final Long deletedTagId = getTagIdByTagName(event.getEntity().getName());
        removeTagFromCombo(deletedTagId);
    }

}
