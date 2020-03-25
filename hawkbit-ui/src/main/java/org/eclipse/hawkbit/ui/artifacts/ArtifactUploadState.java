/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.ui.artifacts.details.ArtifactDetailsGridLayoutUiState;
import org.eclipse.hawkbit.ui.artifacts.smtable.SoftwareModuleGridLayoutUiState;
import org.eclipse.hawkbit.ui.artifacts.smtype.filter.SMTypeFilterLayoutUiState;
import org.eclipse.hawkbit.ui.artifacts.upload.FileUploadId;
import org.eclipse.hawkbit.ui.artifacts.upload.FileUploadProgress;
import org.eclipse.hawkbit.ui.artifacts.upload.FileUploadProgress.FileUploadStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;

/**
 * User status of Artifact upload.
 */
@VaadinSessionScope
@SpringComponent
public class ArtifactUploadState implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(ArtifactUploadState.class);

    private final SMTypeFilterLayoutUiState smTypeFilterLayoutUiState;
    private final SoftwareModuleGridLayoutUiState smGridLayoutUiState;
    private final ArtifactDetailsGridLayoutUiState artifactDetailsGridLayoutUiState;

    private boolean statusPopupMinimized;

    /**
     * Map that holds all files that were selected for upload. They remain in
     * the list even if upload fails, succeeds or is aborted by the user.
     */
    private Map<FileUploadId, FileUploadProgress> overallFilesInUploadProcess;

    public ArtifactUploadState() {
        this.smTypeFilterLayoutUiState = new SMTypeFilterLayoutUiState();
        this.smGridLayoutUiState = new SoftwareModuleGridLayoutUiState();
        this.artifactDetailsGridLayoutUiState = new ArtifactDetailsGridLayoutUiState();
    }

    public void setStatusPopupMinimized(final boolean statusPopupMinimized) {
        this.statusPopupMinimized = statusPopupMinimized;
    }

    public boolean isStatusPopupMinimized() {
        return statusPopupMinimized;
    }

    public SMTypeFilterLayoutUiState getSmTypeFilterLayoutUiState() {
        return smTypeFilterLayoutUiState;
    }

    public SoftwareModuleGridLayoutUiState getSmGridLayoutUiState() {
        return smGridLayoutUiState;
    }

    public ArtifactDetailsGridLayoutUiState getArtifactDetailsGridLayoutUiState() {
        return artifactDetailsGridLayoutUiState;
    }

    public void removeFilesFromOverallUploadProcessList(final Collection<FileUploadId> filesToRemove) {
        getOverallFilesInUploadProcessMap().keySet().removeAll(filesToRemove);
    }

    public Set<FileUploadId> getAllFileUploadIdsFromOverallUploadProcessList() {
        return Collections.unmodifiableSet(getOverallFilesInUploadProcessMap().keySet());
    }

    public Collection<FileUploadProgress> getAllFileUploadProgressValuesFromOverallUploadProcessList() {
        return Collections.unmodifiableCollection(getOverallFilesInUploadProcessMap().values());
    }

    public Set<FileUploadId> getFilesInFailedState() {
        return Collections.unmodifiableSet(getFailedUploads());
    }

    public FileUploadProgress getFileUploadProgress(final FileUploadId fileUploadId) {
        return getOverallFilesInUploadProcessMap().get(fileUploadId);
    }

    public void updateFileUploadProgress(final FileUploadId fileUploadId, final FileUploadProgress fileUploadProgress) {
        getOverallFilesInUploadProcessMap().put(fileUploadId, fileUploadProgress);
    }

    public boolean isFileInUploadState(final FileUploadId fileUploadId) {
        return getOverallFilesInUploadProcessMap().containsKey(fileUploadId);
    }

    public boolean isFileInUploadState(final String filename, final SoftwareModule softwareModule) {
        return isFileInUploadState(new FileUploadId(filename, softwareModule));
    }

    public boolean isAtLeastOneUploadInProgress() {
        return getInProgressCount() > 0;
    }

    public boolean areAllUploadsFinished() {
        return getInProgressCount() == 0;
    }

    private int getInProgressCount() {
        final int succeededUploadCount = getSucceededUploads().size();
        final int failedUploadCount = getFailedUploads().size();
        final int overallUploadCount = getOverallFilesInUploadProcessMap().size();

        final int inProgressCount = overallUploadCount - failedUploadCount - succeededUploadCount;

        assertFileStateConsistency(inProgressCount, overallUploadCount, succeededUploadCount, failedUploadCount);

        return inProgressCount;
    }

    private static void assertFileStateConsistency(final int inProgressCount, final int overallUploadCount,
            final int succeededUploadCount, final int failedUploadCount) {
        if (inProgressCount < 0) {
            LOG.error("IllegalState: \n{}",
                    getStateListLogMessage(overallUploadCount, succeededUploadCount, failedUploadCount));
        }
    }

    private static String getStateListLogMessage(final int overallUploadCount, final int succeededUploadCount,
            final int failedUploadCount) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("Overall uploads: " + overallUploadCount);
        buffer.append("| succeeded uploads: " + succeededUploadCount);
        buffer.append("| Failed Uploads: " + failedUploadCount);
        return buffer.toString();
    }

    public void clearFileStates() {
        getOverallFilesInUploadProcessMap().clear();
    }

    /**
     * Clears all temp data collected while uploading files.
     */
    public void clearUploadTempData() {
        LOG.debug("Cleaning up temp data...");
        // delete file system zombies
        for (final FileUploadProgress fileUploadProgress : getAllFileUploadProgressValuesFromOverallUploadProcessList()) {
            if (!StringUtils.isBlank(fileUploadProgress.getFilePath())) {
                final boolean deleted = FileUtils.deleteQuietly(new File(fileUploadProgress.getFilePath()));
                if (!deleted) {
                    LOG.warn("TempFile was not deleted: {}", fileUploadProgress.getFilePath());
                }
            }
        }
        clearFileStates();
    }

    /**
     * Checks if an upload is in progress for the given Software Module
     * 
     * @param softwareModuleId
     *            id of the software module
     * @return boolean
     */
    public boolean isUploadInProgressForSelectedSoftwareModule(final Long softwareModuleId) {
        for (final FileUploadId fileUploadId : getAllFileUploadIdsFromOverallUploadProcessList()) {
            if (fileUploadId.getSoftwareModuleId().equals(softwareModuleId)) {
                return true;
            }
        }
        return false;
    }

    private Map<FileUploadId, FileUploadProgress> getOverallFilesInUploadProcessMap() {
        if (overallFilesInUploadProcess == null) {
            overallFilesInUploadProcess = Maps.newConcurrentMap();
        }
        return overallFilesInUploadProcess;
    }

    private Set<FileUploadId> getFailedUploads() {
        final Collection<FileUploadProgress> allFileUploadProgressObjects = getOverallFilesInUploadProcessMap()
                .values();
        final Set<FileUploadId> failedFileUploads = Sets.newHashSet();
        for (final FileUploadProgress fileUploadProgress : allFileUploadProgressObjects) {
            if (fileUploadProgress.getFileUploadStatus() == FileUploadStatus.UPLOAD_FAILED) {
                failedFileUploads.add(fileUploadProgress.getFileUploadId());
            }
        }
        return failedFileUploads;
    }

    private Set<FileUploadId> getSucceededUploads() {
        final Collection<FileUploadProgress> allFileUploadProgressObjects = getOverallFilesInUploadProcessMap()
                .values();
        final Set<FileUploadId> succeededFileUploads = Sets.newHashSet();
        for (final FileUploadProgress fileUploadProgress : allFileUploadProgressObjects) {
            if (fileUploadProgress.getFileUploadStatus() == FileUploadStatus.UPLOAD_SUCCESSFUL) {
                succeededFileUploads.add(fileUploadProgress.getFileUploadId());
            }
        }
        return succeededFileUploads;
    }
}