/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.amqp;

import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.InvalidTargetAttributeException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

/**
 * An error handler for all invalid target operations resulting from AMQP.
 */
public class InvalidTargetOperationsExceptionHandler implements AmqpErrorHandler {

    @Override
    public void doHandle(final Throwable t, final AmqpErrorHandlerChain chain) {
        if (t instanceof InvalidTargetAttributeException || t instanceof EntityNotFoundException) {
            throw new AmqpRejectAndDontRequeueException(t.getMessage());
        } else {
            chain.handle(t);
        }
    }
}
