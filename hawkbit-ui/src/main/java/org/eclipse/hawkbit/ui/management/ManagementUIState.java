/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management;

import java.io.Serializable;

import org.eclipse.hawkbit.ui.management.actionhistory.ActionHistoryGridLayoutUiState;
import org.eclipse.hawkbit.ui.management.bulkupload.TargetBulkUploadUiState;
import org.eclipse.hawkbit.ui.management.dstable.DistributionGridLayoutUiState;
import org.eclipse.hawkbit.ui.management.dstag.filter.DistributionTagLayoutUiState;
import org.eclipse.hawkbit.ui.management.targettable.TargetGridLayoutUiState;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterLayoutUiState;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;

/**
 * User action on management UI.
 */
@VaadinSessionScope
@SpringComponent
public class ManagementUIState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState;
    private final TargetGridLayoutUiState targetGridLayoutUiState;
    private final TargetBulkUploadUiState targetBulkUploadUiState;
    private final DistributionGridLayoutUiState distributionGridLayoutUiState;
    private final DistributionTagLayoutUiState distributionTagLayoutUiState;
    private final ActionHistoryGridLayoutUiState actionHistoryGridLayoutUiState;

    ManagementUIState() {
        this.targetTagFilterLayoutUiState = new TargetTagFilterLayoutUiState();
        this.targetGridLayoutUiState = new TargetGridLayoutUiState();
        this.targetBulkUploadUiState = new TargetBulkUploadUiState();
        this.distributionGridLayoutUiState = new DistributionGridLayoutUiState();
        this.distributionTagLayoutUiState = new DistributionTagLayoutUiState();
        this.actionHistoryGridLayoutUiState = new ActionHistoryGridLayoutUiState();

        init();
    }

    private void init() {
        distributionTagLayoutUiState.setHidden(true);
    }

    public TargetTagFilterLayoutUiState getTargetTagFilterLayoutUiState() {
        return targetTagFilterLayoutUiState;
    }

    public TargetGridLayoutUiState getTargetGridLayoutUiState() {
        return targetGridLayoutUiState;
    }

    public DistributionGridLayoutUiState getDistributionGridLayoutUiState() {
        return distributionGridLayoutUiState;
    }

    public DistributionTagLayoutUiState getDistributionTagLayoutUiState() {
        return distributionTagLayoutUiState;
    }

    public ActionHistoryGridLayoutUiState getActionHistoryGridLayoutUiState() {
        return actionHistoryGridLayoutUiState;
    }

    public TargetBulkUploadUiState getTargetBulkUploadUiState() {
        return targetBulkUploadUiState;
    }
}