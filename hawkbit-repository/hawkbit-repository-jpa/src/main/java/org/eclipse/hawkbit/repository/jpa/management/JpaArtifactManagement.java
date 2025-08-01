/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.repository.jpa.management;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.hawkbit.repository.artifact.ArtifactRepository;
import org.eclipse.hawkbit.repository.artifact.exception.ArtifactStoreException;
import org.eclipse.hawkbit.repository.artifact.exception.HashNotMatchException;
import org.eclipse.hawkbit.repository.artifact.model.AbstractDbArtifact;
import org.eclipse.hawkbit.repository.artifact.model.DbArtifact;
import org.eclipse.hawkbit.repository.artifact.model.DbArtifactHash;
import org.eclipse.hawkbit.repository.artifact.encryption.ArtifactEncryptionService;
import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.repository.QuotaManagement;
import org.eclipse.hawkbit.repository.exception.ArtifactDeleteFailedException;
import org.eclipse.hawkbit.repository.exception.ArtifactUploadFailedException;
import org.eclipse.hawkbit.repository.exception.EntityAlreadyExistsException;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.InsufficientPermissionException;
import org.eclipse.hawkbit.repository.exception.InvalidMD5HashException;
import org.eclipse.hawkbit.repository.exception.InvalidSHA1HashException;
import org.eclipse.hawkbit.repository.exception.InvalidSHA256HashException;
import org.eclipse.hawkbit.repository.jpa.EncryptionAwareDbArtifact;
import org.eclipse.hawkbit.repository.jpa.JpaManagementHelper;
import org.eclipse.hawkbit.repository.jpa.acm.AccessController;
import org.eclipse.hawkbit.repository.jpa.configuration.Constants;
import org.eclipse.hawkbit.repository.jpa.model.JpaArtifact;
import org.eclipse.hawkbit.repository.jpa.model.JpaSoftwareModule;
import org.eclipse.hawkbit.repository.jpa.model.helper.AfterTransactionCommitExecutorHolder;
import org.eclipse.hawkbit.repository.jpa.repository.LocalArtifactRepository;
import org.eclipse.hawkbit.repository.jpa.repository.SoftwareModuleRepository;
import org.eclipse.hawkbit.repository.jpa.specifications.ArtifactSpecifications;
import org.eclipse.hawkbit.repository.jpa.utils.DeploymentHelper;
import org.eclipse.hawkbit.repository.jpa.utils.FileSizeAndStorageQuotaCheckingInputStream;
import org.eclipse.hawkbit.repository.jpa.utils.QuotaHelper;
import org.eclipse.hawkbit.repository.model.Artifact;
import org.eclipse.hawkbit.repository.model.ArtifactUpload;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.tenancy.TenantAware;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * JPA based {@link ArtifactManagement} implementation.
 */
@Slf4j
@Transactional(readOnly = true)
@Validated
@Service
@ConditionalOnBooleanProperty(prefix = "hawkbit.jpa", name = { "enabled", "artifact-management" }, matchIfMissing = true)
public class JpaArtifactManagement implements ArtifactManagement {

    private final EntityManager entityManager;
    private final PlatformTransactionManager txManager;
    private final LocalArtifactRepository localArtifactRepository;
    private final SoftwareModuleRepository softwareModuleRepository;
    @Nullable
    private final ArtifactRepository artifactRepository;
    private final TenantAware tenantAware;
    private final QuotaManagement quotaManagement;

    protected JpaArtifactManagement(
            final EntityManager entityManager,
            final PlatformTransactionManager txManager,
            final LocalArtifactRepository localArtifactRepository,
            final SoftwareModuleRepository softwareModuleRepository, @Nullable final ArtifactRepository artifactRepository,
            final QuotaManagement quotaManagement,
            final TenantAware tenantAware) {
        this.entityManager = entityManager;
        this.txManager = txManager;
        this.localArtifactRepository = localArtifactRepository;
        this.softwareModuleRepository = softwareModuleRepository;
        this.artifactRepository = artifactRepository;
        this.quotaManagement = quotaManagement;
        this.tenantAware = tenantAware;
    }

    @Override
    public long count() {
        return localArtifactRepository.count();
    }

    @Override
    @Transactional
    @Retryable(retryFor = { ConcurrencyFailureException.class }, maxAttempts = Constants.TX_RT_MAX,
            backoff = @Backoff(delay = Constants.TX_RT_DELAY))
    public Artifact create(final ArtifactUpload artifactUpload) {
        assertArtifactRepositoryAvailable();

        final long moduleId = artifactUpload.getModuleId();
        assertArtifactQuota(moduleId, 1);
        final JpaSoftwareModule softwareModule =
                softwareModuleRepository
                        .findById(moduleId)
                        .orElseThrow(() -> new EntityNotFoundException(SoftwareModule.class, moduleId));

        final String filename = artifactUpload.getFilename();
        final Artifact existing = softwareModule.getArtifactByFilename(filename).orElse(null);
        if (existing != null) {
            if (artifactUpload.isOverrideExisting()) {
                log.debug("overriding existing artifact with new filename {}", filename);
            } else {
                throw new EntityAlreadyExistsException("File with that name already exists in the Software Module");
            }
        }

        // touch it to update the lock revision because we are modifying the
        // DS indirectly, it will, also check UPDATE access
        JpaManagementHelper.touch(entityManager, softwareModuleRepository, softwareModule);

        final AbstractDbArtifact artifact = storeArtifact(artifactUpload, softwareModule.isEncrypted());
        try {
            return storeArtifactMetadata(softwareModule, filename, artifact, existing);
        } catch (final Exception e) {
            artifactRepository.deleteBySha1(tenantAware.getCurrentTenant(), artifact.getHashes().getSha1());
            throw e;
        }
    }

    @Override
    @Transactional
    @Retryable(retryFor = { ConcurrencyFailureException.class }, maxAttempts = Constants.TX_RT_MAX,
            backoff = @Backoff(delay = Constants.TX_RT_DELAY))
    public void delete(final long id) {
        final JpaArtifact toDelete = (JpaArtifact) get(id).orElseThrow(() -> new EntityNotFoundException(Artifact.class, id));

        final JpaSoftwareModule softwareModule = toDelete.getSoftwareModule();
        // clearArtifactBinary checks (unconditionally) software module UPDATE access
        softwareModuleRepository.getAccessController().ifPresent(accessController ->
                accessController.assertOperationAllowed(AccessController.Operation.UPDATE, softwareModule));
        softwareModule.removeArtifact(toDelete);
        softwareModuleRepository.save(softwareModule);

        localArtifactRepository.deleteById(id);

        final String sha1Hash = toDelete.getSha1Hash();
        AfterTransactionCommitExecutorHolder.getInstance().getAfterCommit().afterCommit(() -> clearArtifactBinary(sha1Hash));
    }

    @Override
    public Optional<Artifact> get(final long id) {
        return localArtifactRepository.findById(id).map(Artifact.class::cast);
    }

    @Override
    public Optional<Artifact> getByFilenameAndSoftwareModule(final String filename, final long softwareModuleId) {
        assertSoftwareModuleExists(softwareModuleId);

        return localArtifactRepository.findFirstByFilenameAndSoftwareModuleId(filename, softwareModuleId);
    }

    @Override
    public Optional<Artifact> findFirstBySHA1(final String sha1Hash) {
        return localArtifactRepository.findFirstBySha1Hash(sha1Hash);
    }

    @Override
    public Optional<Artifact> getByFilename(final String filename) {
        return localArtifactRepository.findFirstByFilename(filename);
    }

    @Override
    public Page<Artifact> findBySoftwareModule(final long softwareModuleId, final Pageable pageable) {
        assertSoftwareModuleExists(softwareModuleId);

        return localArtifactRepository
                .findAll(ArtifactSpecifications.bySoftwareModuleId(softwareModuleId), pageable)
                .map(Artifact.class::cast);
    }

    @Override
    public long countBySoftwareModule(final long softwareModuleId) {
        assertSoftwareModuleExists(softwareModuleId);

        return localArtifactRepository.count(ArtifactSpecifications.bySoftwareModuleId(softwareModuleId));
    }

    @SuppressWarnings("java:S2201") // java:S2201 - the idea is to just check if the artifact exists
    @Override
    public Optional<DbArtifact> loadArtifactBinary(final String sha1Hash, final long softwareModuleId, final boolean isEncrypted) {
        assertArtifactRepositoryAvailable();

        assertSoftwareModuleExists(softwareModuleId);

        final String tenant = tenantAware.getCurrentTenant();
        if (artifactRepository.existsByTenantAndSha1(tenant, sha1Hash)) {
            // assert artifact exists and belongs to the software module
            findFirstBySHA1(sha1Hash)
                    // if not found no assertOperationAllowed shall fail
                    .orElseThrow(InsufficientPermissionException::new);

            final DbArtifact dbArtifact = artifactRepository.getArtifactBySha1(tenant, sha1Hash);
            return Optional.ofNullable(
                    isEncrypted ? wrapInEncryptionAwareDbArtifact(softwareModuleId, dbArtifact) : dbArtifact);
        }

        return Optional.empty();
    }

    /**
     * Garbage collects artifact binaries if only referenced by given {@link SoftwareModule#getId()} or {@link SoftwareModule}'s that are
     * marked as deleted.
     * <p/>
     * Software module related UPDATE permission shall be checked by the callers!
     * <p/>
     * Note: Internal method. Shall be called ONLY if @PreAuthorize(SpPermission.SpringEvalExpressions.HAS_AUTH_DELETE_REPOSITORY)
     * has already been checked
     *
     * @param sha1Hash no longer needed
     */
    void clearArtifactBinary(final String sha1Hash) {
        assertArtifactRepositoryAvailable();

        DeploymentHelper.runInNewTransaction(txManager, "clearArtifactBinary", status -> {
            // countBySha1HashAndTenantAndSoftwareModuleDeletedIsFalse will skip ACM checks and will return total count as it should be
            if (localArtifactRepository.countBySha1HashAndTenantAndSoftwareModuleDeletedIsFalse(sha1Hash, tenantAware.getCurrentTenant()) <= 0) { // 1 artifact is the one being deleted!
                // removes the real artifact ONLY AFTER the delete of artifact or software module
                // in local history has passed successfully (caller has permission and no errors)
                AfterTransactionCommitExecutorHolder.getInstance().getAfterCommit().afterCommit(() -> {
                    try {
                        log.debug("deleting artifact from repository {}", sha1Hash);
                        artifactRepository.deleteBySha1(tenantAware.getCurrentTenant(), sha1Hash);
                    } catch (final ArtifactStoreException e) {
                        throw new ArtifactDeleteFailedException(e);
                    }
                });
            } // else there are still other artifacts that need the binary
            return null;
        });
    }

    private AbstractDbArtifact storeArtifact(final ArtifactUpload artifactUpload, final boolean isSmEncrypted) {
        final String tenant = tenantAware.getCurrentTenant();
        final long smId = artifactUpload.getModuleId();
        final InputStream stream = artifactUpload.getInputStream();
        final String fileName = artifactUpload.getFilename();
        final String contentType = artifactUpload.getContentType();
        final String providedSha1 = artifactUpload.getProvidedSha1Sum();
        final String providedMd5 = artifactUpload.getProvidedMd5Sum();
        final String providedSha256 = artifactUpload.getProvidedSha256Sum();

        try (final InputStream wrappedStream = wrapInQuotaStream(
                isSmEncrypted ? wrapInEncryptionStream(smId, stream) : stream)) {
            return artifactRepository.store(tenant, wrappedStream, fileName, contentType,
                    new DbArtifactHash(providedSha1, providedMd5, providedSha256));
        } catch (final ArtifactStoreException | IOException e) {
            throw new ArtifactUploadFailedException(e);
        } catch (final HashNotMatchException e) {
            if (e.getHashFunction().equals(HashNotMatchException.SHA1)) {
                throw new InvalidSHA1HashException(e.getMessage(), e);
            } else if (e.getHashFunction().equals(HashNotMatchException.SHA256)) {
                throw new InvalidSHA256HashException(e.getMessage(), e);
            } else {
                throw new InvalidMD5HashException(e.getMessage(), e);
            }
        }
    }

    private InputStream wrapInEncryptionStream(final long smId, final InputStream stream) {
        return ArtifactEncryptionService.getInstance().encryptArtifact(smId, stream);
    }

    private void assertArtifactQuota(final long moduleId, final int requested) {
        QuotaHelper.assertAssignmentQuota(
                moduleId, requested, quotaManagement.getMaxArtifactsPerSoftwareModule(),
                Artifact.class, SoftwareModule.class,
                // get all artifacts without user context
                softwareModuleId -> localArtifactRepository
                        .count(null, ArtifactSpecifications.bySoftwareModuleId(softwareModuleId)));
    }

    private InputStream wrapInQuotaStream(final InputStream in) {
        final long maxArtifactSize = quotaManagement.getMaxArtifactSize();

        final long currentlyUsed = localArtifactRepository.sumOfNonDeletedArtifactSize().orElse(0L);
        final long maxArtifactSizeTotal = quotaManagement.getMaxArtifactStorage();

        return new FileSizeAndStorageQuotaCheckingInputStream(in, maxArtifactSize,
                maxArtifactSizeTotal - currentlyUsed);
    }

    private DbArtifact wrapInEncryptionAwareDbArtifact(final long softwareModuleId, final DbArtifact dbArtifact) {
        if (dbArtifact == null) {
            return null;
        }
        final ArtifactEncryptionService encryptionService = ArtifactEncryptionService.getInstance();
        return new EncryptionAwareDbArtifact(dbArtifact,
                stream -> encryptionService.decryptArtifact(softwareModuleId, stream),
                encryptionService.encryptionSizeOverhead());
    }

    private Artifact storeArtifactMetadata(final SoftwareModule softwareModule, final String providedFilename,
            final AbstractDbArtifact result, final Artifact existing) {
        final JpaArtifact artifact;
        if (existing == null) {
            artifact = new JpaArtifact(result.getHashes().getSha1(), providedFilename, softwareModule);
        } else {
            artifact = (JpaArtifact) existing;
            artifact.setSha1Hash(result.getHashes().getSha1());
        }
        artifact.setMd5Hash(result.getHashes().getMd5());
        artifact.setSha256Hash(result.getHashes().getSha256());
        artifact.setFileSize(result.getSize());

        log.debug("storing new artifact into repository {}", artifact);
        return localArtifactRepository.save(AccessController.Operation.CREATE, artifact);
    }

    private void assertSoftwareModuleExists(final long softwareModuleId) {
        if (!softwareModuleRepository.existsById(softwareModuleId)) {
            throw new EntityNotFoundException(SoftwareModule.class, softwareModuleId);
        }
    }

    private void assertArtifactRepositoryAvailable() {
        if (artifactRepository == null) {
            throw new UnsupportedOperationException("ArtifactRepository is unavailable");
        }
    }
}