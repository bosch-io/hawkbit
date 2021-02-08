/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.app;

import org.eclipse.hawkbit.repository.test.util.SqlTestDatabase;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "hawkbit.dmf.rabbitmq.enabled=false" })
public abstract class AbstractSecurityTest {

    @ClassRule
    public static final SqlTestDatabase randomDatabaseRule = new SqlTestDatabase();

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mvc;

    @Before
    public void setup() {
        final DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity()).dispatchOptions(true);
        mvc = builder.build();
    }

}