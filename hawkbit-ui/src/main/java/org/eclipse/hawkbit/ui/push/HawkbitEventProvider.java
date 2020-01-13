/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.push;

import java.util.Map;

import org.eclipse.hawkbit.repository.event.TenantAwareEvent;
import org.eclipse.hawkbit.repository.event.remote.DistributionSetDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.DistributionSetTagDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.DistributionSetTypeDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.RolloutDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.SoftwareModuleDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.SoftwareModuleTypeDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.TargetDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.TargetFilterQueryDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.TargetTagDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.CancelTargetAssignmentEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetTagCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetTagUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetTypeCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetTypeUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.SoftwareModuleCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.SoftwareModuleTypeCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.SoftwareModuleTypeUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.SoftwareModuleUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetFilterQueryCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetFilterQueryUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetTagCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetTagUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetUpdatedEvent;
import org.eclipse.hawkbit.ui.push.event.RolloutChangedEvent;
import org.eclipse.hawkbit.ui.push.event.RolloutGroupChangedEvent;

import com.google.common.collect.Maps;

/**
 * The default hawkbit event provider.
 */
public class HawkbitEventProvider implements UIEventProvider {

    private static final Map<Class<? extends TenantAwareEvent>, Class<?>> EVENTS = Maps.newHashMapWithExpectedSize(27);

    static {
        EVENTS.put(TargetCreatedEvent.class, TargetCreatedEventContainer.class);
        EVENTS.put(TargetUpdatedEvent.class, TargetUpdatedEventContainer.class);
        EVENTS.put(TargetDeletedEvent.class, TargetDeletedEventContainer.class);

        EVENTS.put(DistributionSetCreatedEvent.class, DistributionSetCreatedEventContainer.class);
        EVENTS.put(DistributionSetUpdatedEvent.class, DistributionSetUpdatedEventContainer.class);
        EVENTS.put(DistributionSetDeletedEvent.class, DistributionSetDeletedEventContainer.class);

        EVENTS.put(SoftwareModuleCreatedEvent.class, SoftwareModuleCreatedEventContainer.class);
        EVENTS.put(SoftwareModuleUpdatedEvent.class, SoftwareModuleUpdatedEventContainer.class);
        EVENTS.put(SoftwareModuleDeletedEvent.class, SoftwareModuleDeletedEventContainer.class);

        EVENTS.put(TargetTagCreatedEvent.class, TargetTagCreatedEventContainer.class);
        EVENTS.put(TargetTagUpdatedEvent.class, TargetTagUpdatedEventContainer.class);
        EVENTS.put(TargetTagDeletedEvent.class, TargetTagDeletedEventContainer.class);

        EVENTS.put(DistributionSetTagCreatedEvent.class, DistributionSetTagCreatedEventContainer.class);
        EVENTS.put(DistributionSetTagUpdatedEvent.class, DistributionSetTagUpdatedEventContainer.class);
        EVENTS.put(DistributionSetTagDeletedEvent.class, DistributionSetTagDeletedEventContainer.class);

        EVENTS.put(DistributionSetTypeCreatedEvent.class, DistributionSetTypeCreatedEventContainer.class);
        EVENTS.put(DistributionSetTypeUpdatedEvent.class, DistributionSetTypeUpdatedEventContainer.class);
        EVENTS.put(DistributionSetTypeDeletedEvent.class, DistributionSetTypeDeletedEventContainer.class);

        EVENTS.put(SoftwareModuleTypeCreatedEvent.class, SoftwareModuleTypeCreatedEventContainer.class);
        EVENTS.put(SoftwareModuleTypeUpdatedEvent.class, SoftwareModuleTypeUpdatedEventContainer.class);
        EVENTS.put(SoftwareModuleTypeDeletedEvent.class, SoftwareModuleTypeDeletedEventContainer.class);

        EVENTS.put(RolloutChangedEvent.class, RolloutChangeEventContainer.class);
        EVENTS.put(RolloutDeletedEvent.class, RolloutDeletedEventContainer.class);
        EVENTS.put(RolloutGroupChangedEvent.class, RolloutGroupChangedEventContainer.class);

        EVENTS.put(CancelTargetAssignmentEvent.class, CancelTargetAssignmentEventContainer.class);

        EVENTS.put(TargetFilterQueryCreatedEvent.class, TargetFilterQueryCreatedEventContainer.class);
        EVENTS.put(TargetFilterQueryUpdatedEvent.class, TargetFilterQueryUpdatedEventContainer.class);
        EVENTS.put(TargetFilterQueryDeletedEvent.class, TargetFilterQueryDeletedEventContainer.class);
    }

    @Override
    public Map<Class<? extends TenantAwareEvent>, Class<?>> getEvents() {
        return EVENTS;
    }

}
