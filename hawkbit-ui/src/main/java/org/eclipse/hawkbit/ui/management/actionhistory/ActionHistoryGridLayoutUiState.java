/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.actionhistory;

import java.io.Serializable;

public class ActionHistoryGridLayoutUiState implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean maximized;
    private Long selectedActionId;
    private Long selectedActionStatusId;

    public boolean isMaximized() {
        return maximized;
    }

    public void setMaximized(final boolean maximized) {
        this.maximized = maximized;
    }

    public Long getSelectedActionId() {
        return selectedActionId;
    }

    public void setSelectedActionId(final Long selectedActionId) {
        this.selectedActionId = selectedActionId;
    }

    public Long getSelectedActionStatusId() {
        return selectedActionStatusId;
    }

    public void setSelectedActionStatusId(final Long selectedActionStatusId) {
        this.selectedActionStatusId = selectedActionStatusId;
    }
}
