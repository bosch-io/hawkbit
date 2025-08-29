/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.repository.model;

import java.io.InputStream;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.eclipse.hawkbit.repository.ValidString;
import org.eclipse.hawkbit.repository.artifact.model.ArtifactHashes;

/**
 * Use to create a new artifact.
 */
public record ArtifactUpload(
        @NotNull InputStream inputStream,
        String contentType, long filesize,
        ArtifactHashes hash,
        long moduleId,
        @NotEmpty @ValidString String filename,
        boolean overrideExisting) {}