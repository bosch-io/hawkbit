/**
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.autoconfigure.repository.artifact.fs;

import org.eclipse.hawkbit.repository.artifact.fs.FileArtifactProperties;
import org.eclipse.hawkbit.repository.artifact.fs.FileArtifactRepository;
import org.eclipse.hawkbit.repository.artifact.ArtifactRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for the {@link FileArtifactRepository}.
 */
@Configuration
@EnableConfigurationProperties(FileArtifactProperties.class)
public class ArtifactFilesystemConfiguration {

    /**
     * @param artifactFilesystemProperties the artifact file system properties
     * @return Default {@link ArtifactRepository} implementation.
     */
    @Bean
    @ConditionalOnMissingBean
    public ArtifactRepository artifactRepository(final FileArtifactProperties artifactFilesystemProperties) {
        return new FileArtifactRepository(artifactFilesystemProperties);
    }
}