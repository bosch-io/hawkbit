package org.eclipse.hawkbit.ui.rollout.rollout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SearchFilterEventPayload;
import org.eclipse.hawkbit.ui.common.event.View;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

/**
 * Listener for events directed to the {@link RolloutGridLayout} view
 *
 */
public class RolloutGridLayoutEventListener {
    private final RolloutGridLayout rolloutGridLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    /**
     * Constructor
     * 
     * @param rolloutGridLayout
     *            The element that is called when receiving an event
     * @param eventBus
     *            The bus to listen on
     */
    public RolloutGridLayoutEventListener(final RolloutGridLayout rolloutGridLayout, final UIEventBus eventBus) {
        this.rolloutGridLayout = rolloutGridLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new SearchFilterListener());
        eventListeners.add(new EnityModifiedListener());
    }

    private class SearchFilterListener {
        public SearchFilterListener() {
            eventBus.subscribe(this, EventTopics.SEARCH_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onSearchFilter(final SearchFilterEventPayload payload) {
            if (payload.getView() != View.ROLLOUT || payload.getLayout() != rolloutGridLayout.getLayout()) {
                return;
            }

            rolloutGridLayout.filterGridByName(payload.getFilter());
        }
    }

    private class EnityModifiedListener {
        public EnityModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onRolloutModified(final EntityModifiedEventPayload eventPayload) {
            if (!ProxyRollout.class.equals(eventPayload.getEntityType())) {
                return;
            }

            final EntityModifiedEventType modificationType = eventPayload.getEntityModifiedEventType();
            // TODO: bug: first comes Removed and then Updated
            if (modificationType == EntityModifiedEventType.ENTITY_ADDED
                    || modificationType == EntityModifiedEventType.ENTITY_REMOVED) {
                rolloutGridLayout.refreshGrid();
            } else if (modificationType == EntityModifiedEventType.ENTITY_UPDATED) {
                rolloutGridLayout.refreshGridItems(eventPayload.getEntityIds());
            }
        }
    }

    /**
     * unsubscribe all listeners
     */
    public void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }

}
