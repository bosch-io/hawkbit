/**
 * Copyright (c) 2020 Microsoft and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.test.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.util.StringUtils;

/**
 * A {@link TestExecutionListener} for creating and dropping MySql schemas if
 * tests are setup with MySql.
 */
public class PostgreSqlTestDatabase extends AbstractSqlTestDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSqlTestDatabase.class);
    protected static final String POSTGRESQL_URI_PATTERN = "jdbc:postgresql://{host}:{port}/{path}?currentSchema={db}*";

    @Override
    protected boolean isApplicable() {
        return "POSTGRESQL".equals(System.getProperty("spring.jpa.database")) //
                && MATCHER.match(POSTGRESQL_URI_PATTERN, URI)  //
                && !StringUtils.isEmpty(getSchemaName());
    }

    @Override
    protected String createSchema() {
        final String schemaName = getSchemaName();
        LOGGER.info("Creating mysql schema {} if not existing", schemaName);

        executeStatement(URI.split("\\?currentSchema=")[0], schemaName, "CREATE SCHEMA IF NOT EXISTS " + schemaName + ";");
        return schemaName;
    }

    private static String getSchemaName() {
        return MATCHER.extractUriTemplateVariables(POSTGRESQL_URI_PATTERN, URI).get("db");
    }
}
