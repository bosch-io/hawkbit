/**
 * Copyright (c) 2022 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.model;

import java.io.Serializable;

/**
 * {@link AutoConfirmationStatus} of a {@link Target}.
 *
 */
public interface AutoConfirmationStatus extends Serializable {

    /**
     * For which target this status is corresponding to.
     *
     * @return the {@link Target}
     */
    Target getTarget();

    /**
     * The user who initiated the auto confirmation.
     *
     * @return the user
     */
    String getInitiator();

    /**
     * Unix timestamp of the activation.
     *
     * @return activation time as unix timestamp
     */
    long getActivatedAt();

    /**
     * Optional value, which can be set during activation.
     *
     * @return the remark
     */
    String getRemark();

    String constructActionMessage();

}
