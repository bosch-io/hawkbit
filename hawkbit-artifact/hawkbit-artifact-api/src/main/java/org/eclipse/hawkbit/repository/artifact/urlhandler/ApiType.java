/**
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.repository.artifact.urlhandler;

/**
 * hawkBit API type.
 */
public enum ApiType {

    /**
     * Support for Device Management Federation API.
     */
    DMF,

    /**
     * Support for Direct Device Integration API.
     */
    DDI,

    /**
     * Support for Management API.
     */
    MGMT
}