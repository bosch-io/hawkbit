/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.exception;

/**
 * A chain where an event is passed through all handlers until one is found that
 * can process the event.
 *
 * @param <T>
 *            generic type of event.
 */
public interface EventHandlerChain<T> {

    /**
     * Pass the event and search for a suitable handler
     *
     * @param event
     *            the event
     */
    void doHandle(final T event);
}
