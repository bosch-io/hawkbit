/**
 * Copyright (c) 2025 Bosch Digital GmbH, Germany. All rights reserved.
 */
package org.eclipse.hawkbit.repository.artifact.model;

import java.util.Objects;

import lombok.Data;

/**
 * Info for an  imported artifact binary.
 */
@Data
public class StoredArtifactInfo {

    private final String contentType;
    private final long size;
    private final ArtifactHashes hashes;

    public StoredArtifactInfo(final String contentType, final long size, final ArtifactHashes hashes) {
        this.hashes = Objects.requireNonNull(hashes, "Hashes cannot be null");
        this.contentType = contentType;
        this.size = size;
    }
}