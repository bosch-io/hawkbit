/**
 * Copyright (c) 2021 Bosch.IO GmbH and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.amqp;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Test;
import org.springframework.util.ErrorHandler;

@Feature("Unit Tests - Delegating Conditional Error Handler")
@Story("Delegating Conditional Error Handler")
class DelegatingAmqpErrorHandlerTest {

    private final DelegatingConditionalErrorHandler delegatingConditionalErrorHandler =
            new DelegatingConditionalErrorHandler(
                    List.of(new IllegalArgumentExceptionHandler(), new IndexOutOfBoundsExceptionHandler()),
                    new DefaultErrorHandler());

    @Test
    @Description("Verifies that with a list of conditional error handlers, the error is delegated to specific handler.")
    void verifyDelegationHandling() {
        final Throwable error = new Throwable(new IllegalArgumentException());
        assertThatExceptionOfType(IllegalArgumentException.class)
                .as("Expected handled exception to be of type IllegalArgumentException")
                .isThrownBy(() -> delegatingConditionalErrorHandler.handleError(error));
    }

    @Test
    @Description("Verifies that default handler is used if no handlers are defined for the specific exception.")
    void verifyDefaultDelegationHandling() {
        final Throwable error = new Throwable(new NullPointerException());
        assertThatExceptionOfType(RuntimeException.class)
                .as("Expected handled exception to be of type RuntimeException")
                .isThrownBy(() -> delegatingConditionalErrorHandler.handleError(error));
    }

    // Test class
    static class IllegalArgumentExceptionHandler implements AmqpErrorHandler {

        @Override
        public void doHandle(final Throwable t, final AmqpErrorHandlerChain chain) {
            if (t.getCause() instanceof IllegalArgumentException) {
                throw new IllegalArgumentException(t.getMessage());
            } else {
                chain.handle(t);
            }
        }
    }

    // Test class
    static class IndexOutOfBoundsExceptionHandler implements AmqpErrorHandler {

        @Override
        public void doHandle(final Throwable t, final AmqpErrorHandlerChain chain) {
            if (t.getCause() instanceof IndexOutOfBoundsException) {
                throw new IndexOutOfBoundsException(t.getMessage());
            } else {
                chain.handle(t);
            }
        }
    }

    // Test class
    static class DefaultErrorHandler implements ErrorHandler {

        @Override
        public void handleError(final Throwable t) {
            throw new RuntimeException(t);
        }
    }
}