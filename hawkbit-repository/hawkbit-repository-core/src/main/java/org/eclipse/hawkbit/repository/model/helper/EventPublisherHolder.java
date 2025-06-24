/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.repository.model.helper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;

/**
 * A singleton bean which holds the event publisher and service origin id in order to publish remote application events.
 * It can be used in beans not instantiated by spring e.g. JPA entities which cannot be auto-wired.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("java:S6548") // java:S6548 - singleton holder ensures static access to spring resources in some places
public final class EventPublisherHolder {

    private static final EventPublisherHolder SINGLETON = new EventPublisherHolder();

    @Autowired
    ApplicationContext applicationContext;

    @Getter
    private ApplicationEventPublisher eventPublisher;

    /**
     * @return the event publisher holder singleton instance
     */
    public static EventPublisherHolder getInstance() {
        return SINGLETON;
    }

    @Autowired // spring setter injection
    public void setApplicationEventPublisher(final ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }


    public String getApplicationId() {
        return applicationContext.getId();
    }
}