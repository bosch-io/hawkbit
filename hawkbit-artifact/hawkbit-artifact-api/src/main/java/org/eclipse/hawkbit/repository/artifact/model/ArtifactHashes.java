/**
 * Copyright (c) 2025 Bosch Digital GmbH, Germany. All rights reserved.
 */
package org.eclipse.hawkbit.repository.artifact.model;

/**
 * Representation of artifact hashes.
 */
public record ArtifactHashes(String sha1, String md5, String sha256) {}