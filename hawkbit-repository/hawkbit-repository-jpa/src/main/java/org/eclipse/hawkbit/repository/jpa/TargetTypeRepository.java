/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa;

import org.eclipse.hawkbit.repository.jpa.model.JpaTargetType;
import org.eclipse.hawkbit.repository.model.TargetType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * {@link PagingAndSortingRepository} for {@link JpaTargetType}.
 *
 */
@Transactional(readOnly = true)
public interface TargetTypeRepository
        extends BaseEntityRepository<JpaTargetType, Long>, JpaSpecificationExecutor<JpaTargetType> {

    /**
     * @param tenant
     *          Tenant
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM JpaTargetType t WHERE t.tenant = :tenant")
    void deleteByTenant(@Param("tenant") String tenant);

    /**
     * @param ids
     *          List of ID
     * @return Taget type list
     */
    @Override
    @Query("SELECT d FROM JpaTargetType d WHERE d.id IN ?1")
    List<JpaTargetType> findAllById(Iterable<Long> ids);

    @Query(value = "SELECT COUNT (t.id) FROM JpaDistributionSetType t JOIN t.compatibleToTargetTypes tt WHERE tt.id = :id")
    long countDsSetTypesById(@Param("id") Long id);

    /**
     *
     * @param dsTypeId
     *            to search for
     * @return all {@link TargetType}s in the repository with given
     *         {@link TargetType#getName()}
     */
    @Query(value = "SELECT DISTINCT t FROM JpaTargetType t JOIN t.distributionSetTypes dst WHERE dst.id = :id")
    List<JpaTargetType> findByDsType(@Param("id") Long dsTypeId);

    /**
     *
     * @param name
     *            to search for
     * @return all {@link TargetType}s in the repository with given
     *         {@link TargetType#getName()}
     */
    Optional<TargetType> findByName(String name);
}
