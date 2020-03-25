/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener;

import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class MasterEntityChangedListener<M extends ProxyIdentifiableEntity> implements EventListener {
    private final UIEventBus eventBus;
    private final List<MasterEntityAwareComponent<M>> masterEntityAwareComponents;
    private final View masterEntityView;
    private final Layout masterEntityLayout;

    public MasterEntityChangedListener(final UIEventBus eventBus,
            final List<MasterEntityAwareComponent<M>> masterEntityAwareComponents, final View masterEntityView,
            final Layout masterEntityLayout) {
        this.eventBus = eventBus;
        this.masterEntityAwareComponents = masterEntityAwareComponents;
        this.masterEntityView = masterEntityView;
        this.masterEntityLayout = masterEntityLayout;

        eventBus.subscribe(this, EventTopics.SELECTION_CHANGED);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onMasterEntityChangedEvent(final SelectionChangedEventPayload<M> eventPayload) {
        if (eventPayload.getView() != masterEntityView || eventPayload.getLayout() != masterEntityLayout) {
            return;
        }

        if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
            onMasterEntityChanged(eventPayload.getEntity());
        } else {
            onMasterEntityChanged(null);
        }
    }

    private void onMasterEntityChanged(final M masterEntity) {
        masterEntityAwareComponents.forEach(component -> component.masterEntityChanged(masterEntity));
    }

    @Override
    public void unsubscribe() {
        eventBus.unsubscribe(this);
    }
}