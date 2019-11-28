/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.upload;

import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.MultipartConfigElement;

import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Html5File;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.FileDropHandler;
import com.vaadin.ui.dnd.FileDropTarget;
import com.vaadin.ui.dnd.event.FileDropEvent;

/**
 * Container for drag and drop area in the upload view.
 */
public class UploadDropAreaLayout extends CustomComponent {
    private static final long serialVersionUID = 1L;

    private final VaadinMessageSource i18n;
    private final UINotification uiNotification;

    private final ArtifactUploadState artifactUploadState;

    private final transient ArtifactManagement artifactManagement;
    private final transient SoftwareModuleManagement softwareManagement;

    private final transient MultipartConfigElement multipartConfigElement;
    private final transient Lock uploadLock = new ReentrantLock();

    private final UploadProgressButtonLayout uploadButtonLayout;
    private VerticalLayout dropAreaLayout;

    /**
     * Creates a new {@link UploadDropAreaLayout} instance.
     * 
     * @param i18n
     *            the {@link VaadinMessageSource}
     * @param eventBus
     *            the {@link EventBus} used to send/retrieve events
     * @param uiNotification
     *            {@link UINotification} for showing notifications
     * @param artifactUploadState
     *            the {@link ArtifactUploadState} for state information
     * @param multipartConfigElement
     *            the {@link MultipartConfigElement}
     * @param softwareManagement
     *            the {@link SoftwareModuleManagement} for retrieving the
     *            {@link SoftwareModule}
     * @param artifactManagement
     *            the {@link ArtifactManagement} for storing the uploaded
     *            artifacts
     */
    public UploadDropAreaLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final UINotification uiNotification, final ArtifactUploadState artifactUploadState,
            final MultipartConfigElement multipartConfigElement, final SoftwareModuleManagement softwareManagement,
            final ArtifactManagement artifactManagement) {
        this.i18n = i18n;
        this.uiNotification = uiNotification;
        this.artifactUploadState = artifactUploadState;
        this.multipartConfigElement = multipartConfigElement;
        this.softwareManagement = softwareManagement;
        this.artifactManagement = artifactManagement;

        this.uploadButtonLayout = new UploadProgressButtonLayout(i18n, eventBus, artifactUploadState,
                multipartConfigElement, artifactManagement, softwareManagement, uploadLock);

        buildLayout();
    }

    public void updateMasterEntityFilter(final Long masterEntityId) {
        dropAreaLayout.setEnabled(masterEntityId != null);
        uploadButtonLayout.updateMasterEntityFilter(masterEntityId);
    }

    private void buildLayout() {
        dropAreaLayout = new VerticalLayout();
        dropAreaLayout.setId(UIComponentIdProvider.UPLOAD_ARTIFACT_FILE_DROP_LAYOUT);
        dropAreaLayout.setMargin(false);
        dropAreaLayout.setSpacing(false);
        dropAreaLayout.addStyleName("upload-drop-area-layout-info");
        dropAreaLayout.setEnabled(false);
        dropAreaLayout.setHeightUndefined();

        final Label dropIcon = new Label(VaadinIcons.ARROW_DOWN.getHtml(), ContentMode.HTML);
        dropIcon.addStyleName("drop-icon");
        dropIcon.setWidth(null);
        dropIcon.setCaptionAsHtml(true);
        dropAreaLayout.addComponent(dropIcon);

        final Label dropHereLabel = new Label(i18n.getMessage(UIMessageIdProvider.LABEL_DROP_AREA_UPLOAD));
        dropHereLabel.setWidth(null);
        dropAreaLayout.addComponent(dropHereLabel);

        uploadButtonLayout.setWidth(null);
        uploadButtonLayout.addStyleName("upload-button");
        dropAreaLayout.addComponent(uploadButtonLayout);

        new FileDropTarget<>(dropAreaLayout, new UploadFileDropHandler());

        setCompositionRoot(dropAreaLayout);
    }

    public VerticalLayout getDropAreaLayout() {
        return dropAreaLayout;
    }

    public void onUploadChanged(final FileUploadProgress fileUploadProgress) {
        uploadButtonLayout.onUploadChanged(fileUploadProgress);
    }

    private class UploadFileDropHandler implements FileDropHandler<VerticalLayout> {

        private static final long serialVersionUID = 1L;

        @Override
        public void drop(final FileDropEvent<VerticalLayout> event) {
            if (validate(event)) {
                // selected software module at the time of file drop is
                // considered for upload
                final Long lastSelectedSmId = artifactUploadState.getSmGridLayoutUiState().getSelectedSmId();
                if (lastSelectedSmId != null) {
                    uploadFilesForSoftwareModule(event.getFiles(), lastSelectedSmId);
                }
            }
        }

        private void uploadFilesForSoftwareModule(final Collection<Html5File> files, final Long softwareModuleId) {
            final SoftwareModule softwareModule = softwareManagement.get(softwareModuleId).orElse(null);

            boolean duplicateFound = false;

            for (final Html5File file : files) {
                if (artifactUploadState.isFileInUploadState(file.getFileName(), softwareModule)) {
                    duplicateFound = true;
                } else {
                    file.setStreamVariable(new FileTransferHandlerStreamVariable(file.getFileName(), file.getFileSize(),
                            multipartConfigElement.getMaxFileSize(), file.getType(), softwareModule, artifactManagement,
                            i18n, uploadLock));
                }
            }
            if (duplicateFound) {
                uiNotification.displayValidationError(i18n.getMessage("message.no.duplicateFiles"));
            }
        }

        private boolean validate(final FileDropEvent<VerticalLayout> event) {
            // check if drop is valid.If valid ,check if software module is
            // selected.
            if (!isFilesDropped(event)) {
                uiNotification.displayValidationError(i18n.getMessage("message.drop.onlyFiles"));
                return false;
            }
            return validateSoftwareModuleSelection();
        }

        private boolean isFilesDropped(final FileDropEvent<VerticalLayout> event) {
            return event.getFiles() != null;
        }

        private boolean validateSoftwareModuleSelection() {
            final Long lastSelectedSmId = artifactUploadState.getSmGridLayoutUiState().getSelectedSmId();

            if (lastSelectedSmId == null) {
                uiNotification.displayValidationError(i18n.getMessage("message.error.noSwModuleSelected"));
                return false;
            }
            // if (artifactUploadState.isMoreThanOneSoftwareModulesSelected()) {
            // uiNotification.displayValidationError(i18n.getMessage("message.error.multiSwModuleSelected"));
            // return false;
            // }
            return true;
        }
    }

    public UploadProgressButtonLayout getUploadButtonLayout() {
        return uploadButtonLayout;
    }

}
