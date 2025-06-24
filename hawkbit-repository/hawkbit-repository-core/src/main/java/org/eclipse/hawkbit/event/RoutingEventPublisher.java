package org.eclipse.hawkbit.event;

import java.util.Set;

import org.eclipse.hawkbit.repository.event.remote.AbstractRemoteEvent;
import org.eclipse.hawkbit.repository.event.remote.CancelTargetAssignmentEvent;
import org.eclipse.hawkbit.repository.event.remote.MultiActionEvent;
import org.eclipse.hawkbit.repository.event.remote.TargetAssignDistributionSetEvent;
import org.eclipse.hawkbit.repository.event.remote.TargetAttributesRequestedEvent;
import org.eclipse.hawkbit.repository.event.remote.TargetDeletedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cloud.stream.function.StreamBridge;

public class RoutingEventPublisher implements ApplicationEventPublisher {

    private final StreamBridge streamBridge;
    private final ApplicationEventPublisher delegate;
    private final EventsProperties eventsProperties;

    private static final Set<Class<?>> GROUPED_REMOTE_EVENTS = Set.of(
            TargetAssignDistributionSetEvent.class,
            MultiActionEvent.class,
            CancelTargetAssignmentEvent.class,
            TargetDeletedEvent.class,
            TargetAttributesRequestedEvent.class
    );

    public RoutingEventPublisher(StreamBridge streamBridge, ApplicationEventPublisher delegate, EventsProperties eventsProperties) {
        this.streamBridge = streamBridge;
        this.delegate = delegate;
        this.eventsProperties = eventsProperties;
    }

    @Override
    public void publishEvent(Object event) {
        routeEvent(event);
    }

    @Override
    public void publishEvent(ApplicationEvent event) {
        routeEvent(event);
    }

    private void routeEvent(Object event) {
        if (eventsProperties.isRemoteEnabled() && streamBridge != null && event instanceof AbstractRemoteEvent) {
            // publish remote
            if (GROUPED_REMOTE_EVENTS.contains(event.getClass())) {
                //remote event that must be sent to grouped bus, only 1 replica of a kind to handle/process it
                streamBridge.send(eventsProperties.getRemoteGroupedChannelOut(), event);
            } else {
                //remote broadcast event, send to fanout bus so that all server instances refresh their cache
                streamBridge.send(eventsProperties.getRemoteFanoutChannelOut(), event);
            }
        } else {
            // publish locally
            delegate.publishEvent(event);
        }
    }
}
