/**
 * Copyright (c) 2018 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa.autocleanup;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.jpa.AbstractJpaIntegrationTest;
import org.eclipse.hawkbit.security.SecurityContextTenantAware;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.tenancy.TenantAware;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.locks.DefaultLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

/**
 * Test class for {@link AutoCleanupScheduler}.
 *
 */
@Feature("Component Tests - Repository")
@Story("Auto cleanup scheduler")
@RunWith(MockitoJUnitRunner.class)
public class AutoCleanupSchedulerTest {

    private final AtomicInteger counter = new AtomicInteger();

    private final LockRegistry lockRegistry = new DefaultLockRegistry();
    private final TenantAware tenantAware = new SecurityContextTenantAware();
    private final SystemSecurityContext securityContext = new SystemSecurityContext(tenantAware);

    @Mock
    private SystemManagement systemManagement;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        counter.set(0);
        doAnswer(invocationOnMock -> {
            ((Consumer<String>) invocationOnMock.getArgument(0)).accept("tenant");
            return null;
        }).when(systemManagement).forEachTenant(any());
    }

    @Test
    @Description("Verifies that all cleanup handlers are executed regardless if one of them throws an error")
    public void executeHandlerChain() {

        new AutoCleanupScheduler(systemManagement, securityContext , lockRegistry, Arrays.asList(
                new SuccessfulCleanup(), new SuccessfulCleanup(), new FailingCleanup(), new SuccessfulCleanup())).run();

        assertThat(counter.get()).isEqualTo(4);

    }

    private class SuccessfulCleanup implements CleanupTask {

        @Override
        public void run() {
            counter.incrementAndGet();
        }

        @Override
        public String getId() {
            return "success";
        }

    }

    private class FailingCleanup implements CleanupTask {

        @Override
        public void run() {
            counter.incrementAndGet();
            throw new RuntimeException("cleanup failed");
        }

        @Override
        public String getId() {
            return "success";
        }

    }

}
