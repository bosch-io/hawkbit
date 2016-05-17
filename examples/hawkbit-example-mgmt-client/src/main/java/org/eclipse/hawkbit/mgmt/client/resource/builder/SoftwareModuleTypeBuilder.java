/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.mgmt.client.resource.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.mgmt.json.model.softwaremodule.MgmtSoftwareModuleRequestBodyPost;
import org.eclipse.hawkbit.mgmt.json.model.softwaremoduletype.MgmtSoftwareModuleTypeRequestBodyPost;

import com.google.common.collect.Lists;

/**
 * 
 * Builder pattern for building {@link MgmtSoftwareModuleRequestBodyPost}.
 *
 */
// Exception squid:S1701 - builder pattern
@SuppressWarnings({ "squid:S1701" })
public class SoftwareModuleTypeBuilder {

    private String key;
    private String name;
    private String description;
    private int maxAssignments;

    /**
     * @param key
     *            the key of the software module type
     * @return the builder itself
     */
    public SoftwareModuleTypeBuilder key(final String key) {
        this.key = key;
        return this;
    }

    /**
     * @param name
     *            the name of the software module type
     * @return the builder itself
     */
    public SoftwareModuleTypeBuilder name(final String name) {
        this.name = name;
        return this;
    }

    public SoftwareModuleTypeBuilder description(final String description) {
        this.description = description;
        return this;
    }

    public SoftwareModuleTypeBuilder maxAssignments(final int maxAssignments) {
        this.maxAssignments = maxAssignments;
        return this;
    }

    /**
     * Builds a list with a single entry of
     * {@link MgmtSoftwareModuleTypeRequestBodyPost} which can directly be used
     * in the RESTful-API.
     * 
     * @return a single entry list of
     *         {@link MgmtSoftwareModuleTypeRequestBodyPost}
     */
    public List<MgmtSoftwareModuleTypeRequestBodyPost> build() {
        return Lists.newArrayList(doBuild(key, name));
    }

    /**
     * Builds a list of multiple {@link MgmtSoftwareModuleTypeRequestBodyPost}
     * to create multiple software module types at once. An increasing number
     * will be added to the name and key of the software module type.
     * 
     * @param count
     *            the amount of software module type bodies which should be
     *            created
     * @return a list of {@link MgmtSoftwareModuleTypeRequestBodyPost}
     */
    public List<MgmtSoftwareModuleTypeRequestBodyPost> buildAsList(final int count) {
        final ArrayList<MgmtSoftwareModuleTypeRequestBodyPost> bodyList = Lists.newArrayList();
        for (int index = 0; index < count; index++) {
            bodyList.add(doBuild(key + index, name + index));
        }
        return bodyList;
    }

    private MgmtSoftwareModuleTypeRequestBodyPost doBuild(final String prefixKey, final String prefixName) {
        final MgmtSoftwareModuleTypeRequestBodyPost body = new MgmtSoftwareModuleTypeRequestBodyPost();
        body.setKey(prefixKey);
        body.setName(prefixName);
        body.setDescription(description);
        body.setMaxAssignments(maxAssignments);
        return body;
    }

}