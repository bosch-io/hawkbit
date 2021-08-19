/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.filterlayout;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetType;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Multi button click behaviour of tag filter buttons layout.
 */
public class TargetTypeFilterButtonClick extends AbstractFilterMultiButtonClick<ProxyTargetType> {
    private static final long serialVersionUID = 1L;

    private final transient Consumer<Map<Long, String>> filterChangedCallback;
    private final transient Consumer<ClickBehaviourType> noTagChangedCallback;

    TargetTypeFilterButtonClick(final Consumer<Map<Long, String>> filterChangedCallback,
                                final Consumer<ClickBehaviourType> noTagChangedCallback) {
        this.filterChangedCallback = filterChangedCallback;
        this.noTagChangedCallback = noTagChangedCallback;
    }

    @Override
    protected void filterUnClicked(ProxyTargetType clickedFilter) {

    }

    @Override
    protected void filterClicked(ProxyTargetType clickedFilter) {

    }
}
