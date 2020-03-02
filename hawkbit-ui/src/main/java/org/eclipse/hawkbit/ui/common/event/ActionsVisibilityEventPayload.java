/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class ActionsVisibilityEventPayload extends LayoutAwareEventPayload {
    private final ActionsVisibilityType actionsVisibilityType;

    public ActionsVisibilityEventPayload(final ActionsVisibilityType actionsVisibilityType, final Layout layout,
            final View view) {
        super(layout, view);

        this.actionsVisibilityType = actionsVisibilityType;
    }

    public ActionsVisibilityType getActionsVisibilityType() {
        return actionsVisibilityType;
    }

    public enum ActionsVisibilityType {
        SHOW_EDIT, SHOW_DELETE, HIDE_ALL;
    }
}