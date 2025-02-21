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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.hawkbit.mgmt.json.model.softwaremoduletype.MgmtSoftwareModuleType;
import org.eclipse.hawkbit.mgmt.json.model.softwaremoduletype.MgmtSoftwareModuleTypeRequestBodyPost;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtSoftwareModuleTypeRestApi;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.builder.SoftwareModuleTypeCreate;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.rest.json.model.ResponseList;

/**
 * A mapper which maps repository model to RESTful model representation and back.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MgmtSoftwareModuleTypeMapper {

    static List<SoftwareModuleTypeCreate> smFromRequest(final EntityFactory entityFactory,
            final Collection<MgmtSoftwareModuleTypeRequestBodyPost> smTypesRest) {
        if (smTypesRest == null) {
            return Collections.emptyList();
        }

        return smTypesRest.stream().map(smRest -> fromRequest(entityFactory, smRest)).toList();
    }

    static List<MgmtSoftwareModuleType> toTypesResponse(final Collection<SoftwareModuleType> types) {
        if (types == null) {
            return Collections.emptyList();
        }

        return new ResponseList<>(types.stream().map(MgmtSoftwareModuleTypeMapper::toResponse).toList());
    }

    static MgmtSoftwareModuleType toResponse(final SoftwareModuleType type) {
        final MgmtSoftwareModuleType result = new MgmtSoftwareModuleType();

        MgmtRestModelMapper.mapTypeToType(result, type);
        result.setMaxAssignments(type.getMaxAssignments());
        result.setId(type.getId());

        result.add(linkTo(methodOn(MgmtSoftwareModuleTypeRestApi.class).getSoftwareModuleType(result.getId()))
                .withSelfRel().expand());

        return result;
    }

    private static SoftwareModuleTypeCreate fromRequest(final EntityFactory entityFactory,
            final MgmtSoftwareModuleTypeRequestBodyPost smsRest) {
        return entityFactory.softwareModuleType().create().key(smsRest.getKey()).name(smsRest.getName())
                .description(smsRest.getDescription()).colour(smsRest.getColour())
                .maxAssignments(smsRest.getMaxAssignments());
    }
}