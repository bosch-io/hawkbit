/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.repository;

import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;

/**
 * Repository API constants.
 */
public final class Constants {

    /**
     * {@link SoftwareModuleType#getKey()} of a {@link SoftwareModuleType}
     * generated by repository for every new account for Firmware/Operating
     * System.
     */
    public static final String SMT_DEFAULT_OS_KEY = "os";
    /**
     * {@link SoftwareModuleType#getKey()} of a {@link SoftwareModuleType}
     * generated by repository for every new account for applications.
     */
    public static final String SMT_DEFAULT_APP_KEY = "application";

    /**
     * {@link SoftwareModuleType#getName()} of a {@link SoftwareModuleType}
     * generated by repository for every new account for "Firmware/Operating
     * System" .
     */
    public static final String SMT_DEFAULT_OS_NAME = "OS";
    /**
     * {@link SoftwareModuleType#getName()} of a {@link SoftwareModuleType}
     * generated by repository for every new account for "applications/apps".
     */
    public static final String SMT_DEFAULT_APP_NAME = "Application";

    /**
     * {@link DistributionSetType#getKey()} of a {@link DistributionSetType}
     * generated by repository for every new account that includes
     * {@link #SMT_DEFAULT_OS_KEY} as mandatory module and optional
     * {@link #SMT_DEFAULT_APP_KEY}s.
     */
    public static final String DST_DEFAULT_OS_WITH_APPS_KEY = "os_app";

    /**
     * {@link DistributionSetType#getName()} of a {@link DistributionSetType}
     * generated by repository for every new account that includes
     * {@link #SMT_DEFAULT_OS_KEY} as mandatory module and optional
     * {@link #SMT_DEFAULT_APP_KEY}s.
     */
    public static final String DST_DEFAULT_OS_WITH_APPS_NAME = "OS with app(s)";

    /**
     * {@link DistributionSetType#getKey()} of a {@link DistributionSetType}
     * generated by repository for every new account that includes only
     * {@link #SMT_DEFAULT_OS_KEY} as mandatory module.
     */
    public static final String DST_DEFAULT_OS_ONLY_KEY = "os";

    /**
     * {@link DistributionSetType#getName()} of a {@link DistributionSetType}
     * generated by repository for every new account that includes only
     * {@link #SMT_DEFAULT_OS_KEY} as mandatory module.
     */
    public static final String DST_DEFAULT_OS_ONLY_NAME = "OS only";

    /**
     * {@link DistributionSetType#getKey()} of a {@link DistributionSetType}
     * generated by repository for every new account that includes only
     * {@link #SMT_DEFAULT_APP_KEY} as mandatory module.
     */
    public static final String DST_DEFAULT_APP_ONLY_KEY = "app";

    /**
     * {@link DistributionSetType#getName()} of a {@link DistributionSetType}
     * generated by repository for every new account that includes only
     * {@link #SMT_DEFAULT_APP_KEY} as mandatory module.
     */
    public static final String DST_DEFAULT_APP_ONLY_NAME = "App(s) only";

    private Constants() {
        // Utility class.
    }
}
