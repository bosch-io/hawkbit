/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.eclipse.hawkbit.repository.jpa.model.AbstractJpaTenantAwareBaseEntity;
import org.eclipse.hawkbit.repository.model.BaseEntity;
import org.eclipse.hawkbit.repository.model.TenantAwareBaseEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command repository operations for all {@link TenantAwareBaseEntity}s.
 *
 * @param <T>
 *            type if the entity type
 */
@NoRepositoryBean
@Transactional(readOnly = true)
public interface BaseEntityRepository<T extends AbstractJpaTenantAwareBaseEntity>
        extends PagingAndSortingRepository<T, Long>, CrudRepository<T, Long>,
                JpaSpecificationExecutor<T>, NoCountSliceRepository<T> {

    /**
     * Retrieves an {@link BaseEntity} by its id.
     * 
     * @param id
     *            to search for
     * @return {@link BaseEntity}
     */
    @Override
    Optional<T> findById(Long id);

    /**
     * Overrides
     * {@link org.springframework.data.repository.CrudRepository#saveAll(Iterable)}
     * to return a list of created entities instead of an instance of
     * {@link Iterable} to be able to work with it directly in further code
     * processing instead of converting the {@link Iterable}.
     *
     * @param entities
     *            to persist in the database
     * @return the created entities
     */
    @Override
    @Transactional
    <S extends T> List<S> saveAll(Iterable<S> entities);
}
