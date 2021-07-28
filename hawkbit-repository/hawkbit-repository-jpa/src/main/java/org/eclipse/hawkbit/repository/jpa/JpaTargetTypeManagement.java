/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.TargetTypeManagement;
import org.eclipse.hawkbit.repository.builder.GenericTargetTypeUpdate;
import org.eclipse.hawkbit.repository.builder.TargetTypeCreate;
import org.eclipse.hawkbit.repository.builder.TargetTypeUpdate;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.jpa.builder.JpaTargetTypeCreate;
import org.eclipse.hawkbit.repository.jpa.configuration.Constants;
import org.eclipse.hawkbit.repository.jpa.model.JpaTargetType;
import org.eclipse.hawkbit.repository.jpa.model.JpaTargetType_;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.repository.model.TargetType;
import org.eclipse.hawkbit.repository.rsql.VirtualPropertyReplacer;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * JPA implementation of {@link TargetTypeManagement}.
 *
 */
@Transactional(readOnly = true)
@Validated
public class JpaTargetTypeManagement implements TargetTypeManagement {

    private final TargetTypeRepository targetTypeRepository;
    private final DistributionSetTypeRepository distributionSetTypeRepository;

    private final TargetRepository targetRepository;

    private final VirtualPropertyReplacer virtualPropertyReplacer;
    private final NoCountPagingRepository criteriaNoCountDao;

    private final Database database;

    public JpaTargetTypeManagement(final TargetTypeRepository targetTypeRepository,
                                   final TargetRepository targetRepository,
                                   final DistributionSetTypeRepository distributionSetTypeRepository,
                                   final VirtualPropertyReplacer virtualPropertyReplacer,
                                   final NoCountPagingRepository criteriaNoCountDao,
                                   final Database database) {
        this.targetTypeRepository = targetTypeRepository;
        this.targetRepository = targetRepository;
        this.distributionSetTypeRepository = distributionSetTypeRepository;
        this.virtualPropertyReplacer = virtualPropertyReplacer;
        this.criteriaNoCountDao = criteriaNoCountDao;
        this.database = database;
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
    public void delete(final long targetTypeId) {
        throwExceptionIfTargetTypeDoesNotExist(targetTypeId);
        targetTypeRepository.deleteById(targetTypeId);
    }

    @Override
    public Slice<TargetType> findAll(Pageable pageable) {
        return convertPage(criteriaNoCountDao.findAll(
                (targetRoot, query, cb) -> cb.equal(targetRoot.<Boolean> get(JpaTargetType_.deleted), false),
                pageable, JpaTargetType.class), pageable);
    }

    @Override
    public Page<TargetType> findByTarget(Pageable pageable, String controllerId) {
        return null;
    }

    @Override
    public Page<TargetType> findByRsql(Pageable pageable, String rsqlParam) {
        return null;
    }

    @Override
    public Optional<TargetType> get(long id) {
        // TODO: Add error handler
        Optional<TargetType> type = targetTypeRepository.findById(id).map(targetType -> targetType);
        return type;
    }

    @Override
    public List<TargetType> get(Collection<Long> ids) {
        return null;
    }

    @Override
    public TargetType update(TargetTypeUpdate update) {
        final GenericTargetTypeUpdate typeUpdate = (GenericTargetTypeUpdate) update;

        final JpaTargetType type = findTargetTypeAndThrowExceptionIfNotFound(typeUpdate.getId());

        typeUpdate.getName().ifPresent((type::setName));
        typeUpdate.getDescription().ifPresent(type::setDescription);
        typeUpdate.getColour().ifPresent(type::setColour);

        // todo test this & check if we really need this
        if (typeUpdate.getCompatible().isPresent()) {
            final Collection<Long> currentTargetTypeIds = type.getCompatibleDistributionSetTypes().stream()
                    .map(DistributionSetType::getId).collect(Collectors.toSet());

            typeUpdate.getCompatible().ifPresent(
                    mand -> distributionSetTypeRepository.findAllById(mand).forEach(type::addCompatibleDistributionSetType));
        }

        return targetTypeRepository.save(type);
    }

    @Override
    public DistributionSetType assignOptionalDistributionSetTypes(long targetTypeId, Collection<Long> distributionSetTypeIds) {
        return null;
    }

    @Override
    public DistributionSetType unassignDistributionSetType(long targetTypeId, long distributionSetTypeIds) {
        return null;
    }

    private JpaTargetType findTargetTypeAndThrowExceptionIfNotFound(final Long typeId) {
        return (JpaTargetType) get(typeId)
                .orElseThrow(() -> new EntityNotFoundException(TargetType.class, typeId));
    }

    private void throwExceptionIfTargetTypeDoesNotExist(final Long typeId) {
        if (!targetTypeRepository.existsById(typeId)) {
            throw new EntityNotFoundException(TargetType.class, typeId);
        }
    }

    private static Page<TargetType> convertPage(final Page<JpaTargetType> findAll,
                                                         final Pageable pageable) {
        return new PageImpl<>(Collections.unmodifiableList(findAll.getContent()), pageable, findAll.getTotalElements());
    }

    private static Slice<TargetType> convertPage(final Slice<JpaTargetType> findAll,
                                                          final Pageable pageable) {
        return new PageImpl<>(Collections.unmodifiableList(findAll.getContent()), pageable, 0);
    }

}
