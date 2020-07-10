/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.eclipse.hawkbit.repository.jpa.model.JpaDirectoryGroup;
import org.eclipse.hawkbit.repository.model.DirectoryGroup;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link JpaDirectoryGroup} repository.
 */
@Transactional(readOnly = true)
public interface DirectoryGroupRepository
        extends BaseEntityRepository<JpaDirectoryGroup, Long>, JpaSpecificationExecutor<JpaDirectoryGroup> {

    /**
     * deletes the {@link DirectoryGroup}s with the given names.
     *
     * @param groupName to be deleted
     * @return 1 if group was deleted
     */
    @Modifying
    @Transactional
    Long deleteByName(String groupName);

    /**
     * find {@link DirectoryGroup} by its name.
     *
     * @param groupName to filter on
     * @return the {@link DirectoryGroup} if found
     */
    Optional<DirectoryGroup> findByNameEquals(String groupName);

    /**
     * Checks if group with given name exists.
     *
     * @param groupName to check for
     * @return <code>true</code> is group with given name exists
     */
    @Query("SELECT CASE WHEN COUNT(t)>0 THEN 'true' ELSE 'false' END FROM JpaDirectoryGroup t WHERE t.name=:groupName")
    boolean existsByName(@Param("groupName") String groupName);

    /**
     * Returns all instances of JpaDirectoryGroup.
     *
     * @return all entities
     */
    @Override
    List<JpaDirectoryGroup> findAll();

    /**
     * Deletes all {@link JpaDirectoryGroup} of a given tenant. For safety
     * reasons (this is a "delete everything" query after all) we add the tenant
     * manually to query even if this will by done by {@link EntityManager}
     * anyhow. The DB should take care of optimizing this away.
     *
     * @param tenant to delete data from
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM JpaDirectoryGroup t WHERE t.tenant = :tenant")
    void deleteByTenant(@Param("tenant") String tenant);
}