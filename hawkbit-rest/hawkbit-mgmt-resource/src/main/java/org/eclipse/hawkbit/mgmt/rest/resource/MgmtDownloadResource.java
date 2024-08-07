/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.mgmt.rest.resource;

import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.hawkbit.artifact.repository.ArtifactRepository;
import org.eclipse.hawkbit.artifact.repository.model.AbstractDbArtifact;
import org.eclipse.hawkbit.cache.DownloadArtifactCache;
import org.eclipse.hawkbit.cache.DownloadIdCache;
import org.eclipse.hawkbit.cache.DownloadType;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtDownloadRestApi;
import org.eclipse.hawkbit.rest.util.FileStreamingUtil;
import org.eclipse.hawkbit.rest.util.RequestResponseContextHolder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

/**
 * A resource for download artifacts.
 */
@Slf4j
@RestController
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class MgmtDownloadResource implements MgmtDownloadRestApi {

    private final ArtifactRepository artifactRepository;

    private final DownloadIdCache downloadIdCache;

    MgmtDownloadResource(final ArtifactRepository artifactRepository, final DownloadIdCache downloadIdCache) {
        this.artifactRepository = artifactRepository;
        this.downloadIdCache = downloadIdCache;
    }

    @Override
    @ResponseBody
    public ResponseEntity<InputStream> downloadArtifactByDownloadId(@PathVariable("tenant") final String tenant,
            @PathVariable("downloadId") final String downloadId) {

        try {
            final DownloadArtifactCache artifactCache = downloadIdCache.get(downloadId);
            if (artifactCache == null) {
                log.warn("Download Id {} could not be found", downloadId);
                return ResponseEntity.notFound().build();
            }

            AbstractDbArtifact artifact = null;

            if (DownloadType.BY_SHA1 == artifactCache.getDownloadType()) {
                artifact = artifactRepository.existsByTenantAndSha1(tenant, artifactCache.getId())
                        ? artifactRepository.getArtifactBySha1(tenant, artifactCache.getId())
                        : null;
            } else {
                log.warn("Download Type {} not supported", artifactCache.getDownloadType());
            }

            if (artifact == null) {
                log.warn("Artifact with cached id {} and download type {} could not be found.",
                        artifactCache.getId(), artifactCache.getDownloadType());
                return ResponseEntity.notFound().build();
            }

            return FileStreamingUtil.writeFileResponse(artifact, downloadId, 0L,
                    RequestResponseContextHolder.getHttpServletResponse(),
                    RequestResponseContextHolder.getHttpServletRequest(), null);

        } finally {
            downloadIdCache.evict(downloadId);
        }
    }
}
