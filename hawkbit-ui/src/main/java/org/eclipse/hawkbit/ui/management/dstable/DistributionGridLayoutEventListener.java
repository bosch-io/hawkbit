/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.NoTagFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.PinningChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.PinningChangedEventPayload.PinningChangedEventType;
import org.eclipse.hawkbit.ui.common.event.TagFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.View;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class DistributionGridLayoutEventListener {
    private final DistributionGridLayout distributionGridLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    DistributionGridLayoutEventListener(final DistributionGridLayout distributionGridLayout,
            final UIEventBus eventBus) {
        this.distributionGridLayout = distributionGridLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new TagFilterChangedListener());
        eventListeners.add(new NoTagFilterChangedListener());
        eventListeners.add(new PinnedTargetChangedListener());
    }

    private class TagFilterChangedListener {

        public TagFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.TAG_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onDsTagEvent(final TagFilterChangedEventPayload eventPayload) {
            if (eventPayload.getView() != View.DEPLOYMENT || eventPayload.getLayout() != Layout.DS_TAG_FILTER) {
                return;
            }

            distributionGridLayout.filterGridByTags(eventPayload.getTagNames());
        }
    }

    private class NoTagFilterChangedListener {

        public NoTagFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.NO_TAG_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onDsNoTagEvent(final NoTagFilterChangedEventPayload eventPayload) {
            if (eventPayload.getView() != View.DEPLOYMENT || eventPayload.getLayout() != Layout.DS_TAG_FILTER) {
                return;
            }

            distributionGridLayout.filterGridByNoTag(eventPayload.getIsNoTagActive());
        }
    }

    private class PinnedTargetChangedListener {

        public PinnedTargetChangedListener() {
            eventBus.subscribe(this, EventTopics.PINNING_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetPinEvent(final PinningChangedEventPayload<String> eventPayload) {
            if (!ProxyTarget.class.equals(eventPayload.getEntityType())) {
                return;
            }

            if (eventPayload.getPinningChangedEventType() == PinningChangedEventType.ENTITY_PINNED) {
                distributionGridLayout.filterGridByPinnedTarget(eventPayload.getEntityId());
            } else {
                distributionGridLayout.filterGridByPinnedTarget(null);
            }
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
