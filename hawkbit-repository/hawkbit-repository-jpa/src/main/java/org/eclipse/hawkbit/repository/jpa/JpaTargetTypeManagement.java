/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa;

import org.eclipse.hawkbit.repository.QuotaManagement;
import org.eclipse.hawkbit.repository.TargetTypeFields;
import org.eclipse.hawkbit.repository.TargetTypeManagement;
import org.eclipse.hawkbit.repository.builder.GenericTargetTypeUpdate;
import org.eclipse.hawkbit.repository.builder.TargetTypeCreate;
import org.eclipse.hawkbit.repository.builder.TargetTypeUpdate;
import org.eclipse.hawkbit.repository.exception.AssignmentQuotaExceededException;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.TargetTypeInUseException;
import org.eclipse.hawkbit.repository.jpa.builder.JpaTargetTypeCreate;
import org.eclipse.hawkbit.repository.jpa.configuration.Constants;
import org.eclipse.hawkbit.repository.jpa.model.JpaDistributionSetType;
import org.eclipse.hawkbit.repository.jpa.model.JpaTargetType;
import org.eclipse.hawkbit.repository.jpa.model.JpaTargetType_;
import org.eclipse.hawkbit.repository.jpa.rsql.RSQLUtility;
import org.eclipse.hawkbit.repository.jpa.utils.QuotaHelper;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.repository.model.TargetType;
import org.eclipse.hawkbit.repository.rsql.VirtualPropertyReplacer;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of {@link TargetTypeManagement}.
 *
 */
@Transactional(readOnly = true)
@Validated
public class JpaTargetTypeManagement implements TargetTypeManagement {

    private final TargetTypeRepository targetTypeRepository;
    private final TargetRepository targetRepository;
    private final DistributionSetTypeRepository distributionSetTypeRepository;

    private final VirtualPropertyReplacer virtualPropertyReplacer;

    private final Database database;
    private final QuotaManagement quotaManagement;

    /**
     * Constructor
     *
     * @param targetTypeRepository
     *            Target type repository
     * @param targetRepository
     *            Target repository
     * @param virtualPropertyReplacer
     *            replacer
     * @param database
     *            database
     */
    public JpaTargetTypeManagement(final TargetTypeRepository targetTypeRepository,
            final TargetRepository targetRepository, final DistributionSetTypeRepository distributionSetTypeRepository,
            final VirtualPropertyReplacer virtualPropertyReplacer, final Database database,
            final QuotaManagement quotaManagement) {
        this.targetTypeRepository = targetTypeRepository;
        this.targetRepository = targetRepository;
        this.distributionSetTypeRepository = distributionSetTypeRepository;
        this.virtualPropertyReplacer = virtualPropertyReplacer;
        this.database = database;
        this.quotaManagement = quotaManagement;
    }

    @Override
    public Optional<TargetType> getByName(String name) {
        return targetTypeRepository.findByName(name);
    }

    @Override
    public long count() {
        return targetTypeRepository.count();
    }

    @Override
    public TargetType create(TargetTypeCreate create) {
        final JpaTargetTypeCreate typeCreate = (JpaTargetTypeCreate) create;
        return targetTypeRepository.save(typeCreate.build());
    }

    @Override
    public List<TargetType> create(Collection<TargetTypeCreate> creates) {
        return creates.stream().map(this::create).collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Retryable(include = {
            ConcurrencyFailureException.class }, maxAttempts = Constants.TX_RT_MAX, backoff = @Backoff(delay = Constants.TX_RT_DELAY))
    public void delete(final Long targetTypeId) {
        throwExceptionIfTargetTypeDoesNotExist(targetTypeId);

        if (targetRepository.countByTypeId(targetTypeId) > 0) {
            throw new TargetTypeInUseException("Cannot delete target type that is in use");
        }

        targetTypeRepository.deleteById(targetTypeId);
    }

    @Override
    public Slice<TargetType> findAll(Pageable pageable) {
        return convertPage(targetTypeRepository.findAllWithoutCount(pageable), pageable);
    }

    @Override
    public Page<TargetType> findByTarget(Pageable pageable, String controllerId) {
        return null;
    }

    @Override
    public Page<TargetType> findByRsql(Pageable pageable, String rsqlParam) {
        final Specification<JpaTargetType> spec = RSQLUtility.buildRsqlSpecification(rsqlParam, TargetTypeFields.class,
                virtualPropertyReplacer, database);

        return convertPage(targetTypeRepository.findAll(spec, pageable), pageable);
    }

    @Override
    public Optional<TargetType> get(long id) {
        return targetTypeRepository.findById(id).map(targetType -> targetType);
    }

    @Override
    public List<TargetType> get(Collection<Long> ids) {
        return Collections.unmodifiableList(targetTypeRepository.findAllById(ids));
    }

    @Override
    public TargetType update(TargetTypeUpdate update) {
        final GenericTargetTypeUpdate typeUpdate = (GenericTargetTypeUpdate) update;

        final JpaTargetType type = findTargetTypeAndThrowExceptionIfNotFound(typeUpdate.getId());

        typeUpdate.getName().ifPresent((type::setName));
        typeUpdate.getDescription().ifPresent(type::setDescription);
        typeUpdate.getColour().ifPresent(type::setColour);

        return targetTypeRepository.save(type);
    }

    @Override
    public TargetType assignCompatibleDistributionSetTypes(long targetTypeId, Collection<Long> distributionSetTypeIds) {
        final Collection<JpaDistributionSetType> dsTypes = distributionSetTypeRepository
                .findAllById(distributionSetTypeIds);

        if (dsTypes.size() < distributionSetTypeIds.size()) {
            throw new EntityNotFoundException(DistributionSetType.class, dsTypes,
                    dsTypes.stream().map(DistributionSetType::getId).collect(Collectors.toList()));
        }

        final JpaTargetType type = findTargetTypeAndThrowExceptionIfNotFound(targetTypeId);
        assertDistributionSetTypeQuota(targetTypeId, distributionSetTypeIds.size());

        dsTypes.forEach(type::addCompatibleDistributionSetType);

        return targetTypeRepository.save(type);
    }

    @Override
    public TargetType unassignDistributionSetType(long targetTypeId, long distributionSetTypeId) {
        final JpaTargetType type = findTargetTypeAndThrowExceptionIfNotFound(targetTypeId);
        findDsTypeAndThrowExceptionIfNotFound(distributionSetTypeId);
        type.removeDistributionSetType(distributionSetTypeId);

        return targetTypeRepository.save(type);
    }

    private JpaTargetType findTargetTypeAndThrowExceptionIfNotFound(final Long typeId) {
        return (JpaTargetType) get(typeId).orElseThrow(() -> new EntityNotFoundException(TargetType.class, typeId));
    }

    private JpaDistributionSetType findDsTypeAndThrowExceptionIfNotFound(final Long typeId) {
        List<JpaDistributionSetType> dsTypes = distributionSetTypeRepository.findAllById(Collections.singleton(typeId));
        if (dsTypes.isEmpty()) {
            throw new EntityNotFoundException(DistributionSetType.class, typeId);
        }
        return dsTypes.get(0);
    }

    private void throwExceptionIfTargetTypeDoesNotExist(final Long typeId) {
        if (!targetTypeRepository.existsById(typeId)) {
            throw new EntityNotFoundException(TargetType.class, typeId);
        }
    }

    /**
     * Enforces the quota specifying the maximum number of
     * {@link DistributionSetType}s per {@link TargetType}.
     *
     * @param id
     *            of the target type
     * @param requested
     *            number of distribution set types to check
     *
     * @throws AssignmentQuotaExceededException
     *             if the software module type quota is exceeded
     */
    private void assertDistributionSetTypeQuota(final long id, final int requested) {
        QuotaHelper.assertAssignmentQuota(id, requested, quotaManagement.getMaxDistributionSetTypesPerTargetType(),
                DistributionSetType.class, TargetType.class, targetTypeRepository::countDsSetTypesById);
    }

    private static Page<TargetType> convertPage(final Page<JpaTargetType> findAll, final Pageable pageable) {
        return new PageImpl<>(Collections.unmodifiableList(findAll.getContent()), pageable, findAll.getTotalElements());
    }

    private static Slice<TargetType> convertPage(final Slice<JpaTargetType> findAll, final Pageable pageable) {
        return new PageImpl<>(Collections.unmodifiableList(findAll.getContent()), pageable, 0);
    }

}
