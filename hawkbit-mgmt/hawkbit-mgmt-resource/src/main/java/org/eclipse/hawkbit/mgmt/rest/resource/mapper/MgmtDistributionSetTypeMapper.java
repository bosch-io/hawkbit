/**
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.mgmt.rest.resource.mapper;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.eclipse.hawkbit.mgmt.json.model.distributionsettype.MgmtDistributionSetType;
import org.eclipse.hawkbit.mgmt.json.model.distributionsettype.MgmtDistributionSetTypeRequestBodyPost;
import org.eclipse.hawkbit.mgmt.json.model.softwaremoduletype.MgmtSoftwareModuleTypeAssignment;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtDistributionSetTypeRestApi;
import org.eclipse.hawkbit.mgmt.rest.api.MgmtRestConstants;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.rest.json.model.ResponseList;

/**
 * A mapper which maps repository model to RESTful model representation and back.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MgmtDistributionSetTypeMapper {

    public static List<DistributionSetTypeManagement.Create> smFromRequest(final Collection<MgmtDistributionSetTypeRequestBodyPost> smTypesRest) {
        if (smTypesRest == null) {
            return Collections.emptyList();
        }

        return smTypesRest.stream().map(MgmtDistributionSetTypeMapper::fromRequest).toList();
    }

    public static List<MgmtDistributionSetType> toListResponse(final Collection<? extends DistributionSetType> types) {
        if (types == null) {
            return Collections.emptyList();
        }
        return new ResponseList<>(types.stream().map(MgmtDistributionSetTypeMapper::toResponse).toList());
    }

    public static MgmtDistributionSetType toResponse(final DistributionSetType type) {
        final MgmtDistributionSetType result = new MgmtDistributionSetType();

        MgmtRestModelMapper.mapTypeToType(result, type);
        result.setId(type.getId());

        result.add(linkTo(methodOn(MgmtDistributionSetTypeRestApi.class).getDistributionSetType(result.getId()))
                .withSelfRel().expand());

        return result;
    }

    public static void addLinks(final MgmtDistributionSetType result) {

        result.add(linkTo(methodOn(MgmtDistributionSetTypeRestApi.class).getMandatoryModules(result.getId()))
                .withRel(MgmtRestConstants.DISTRIBUTIONSETTYPE_V1_MANDATORY_MODULES).expand());

        result.add(linkTo(methodOn(MgmtDistributionSetTypeRestApi.class).getOptionalModules(result.getId()))
                .withRel(MgmtRestConstants.DISTRIBUTIONSETTYPE_V1_OPTIONAL_MODULES).expand());
    }

    private static DistributionSetTypeManagement.Create fromRequest(final MgmtDistributionSetTypeRequestBodyPost smsRest) {
        return DistributionSetTypeManagement.Create.builder()
                .key(smsRest.getKey()).name(smsRest.getName())
                .description(smsRest.getDescription()).colour(smsRest.getColour())
                .mandatory(getMandatoryModules(smsRest)).optional(getOptionalModules(smsRest))
                .build();
    }

    private static Collection<Long> getMandatoryModules(final MgmtDistributionSetTypeRequestBodyPost smsRest) {
        return Optional.ofNullable(smsRest.getMandatorymodules())
                .map(modules -> modules.stream().map(MgmtSoftwareModuleTypeAssignment::getId).toList())
                .orElse(Collections.emptyList());
    }

    private static Collection<Long> getOptionalModules(final MgmtDistributionSetTypeRequestBodyPost smsRest) {
        return Optional.ofNullable(smsRest.getOptionalmodules())
                .map(modules -> modules.stream().map(MgmtSoftwareModuleTypeAssignment::getId).toList())
                .orElse(Collections.emptyList());
    }
}