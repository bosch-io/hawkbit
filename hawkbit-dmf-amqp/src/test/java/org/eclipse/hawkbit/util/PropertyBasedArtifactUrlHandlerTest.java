/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.util;

import static org.junit.Assert.assertEquals;

import org.eclipse.hawkbit.AbstractIntegrationTestWithMongoDB;
import org.eclipse.hawkbit.AmqpTestConfiguration;
import org.eclipse.hawkbit.RepositoryApplicationConfiguration;
import org.eclipse.hawkbit.TestConfiguration;
import org.eclipse.hawkbit.TestDataUtil;
import org.eclipse.hawkbit.api.ArtifactUrlHandler;
import org.eclipse.hawkbit.api.UrlProtocol;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.LocalArtifact;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.tenancy.TenantAware;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;

import ru.yandex.qatools.allure.annotations.Description;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Stories;

/**
 * Tests for creating urls to download artifacts.
 */
@Features("Component Tests - Artifact URL Handler")
@Stories("Test to generate the artifact download URL")
@SpringApplicationConfiguration(classes = { RepositoryApplicationConfiguration.class, TestConfiguration.class,
        AmqpTestConfiguration.class })
public class PropertyBasedArtifactUrlHandlerTest extends AbstractIntegrationTestWithMongoDB {

    @Autowired
    private ArtifactUrlHandler urlHandlerProperties;
    @Autowired
    private TenantAware tenantAware;
    private LocalArtifact localArtifact;
    private final String controllerId = "Test";
    private String fileName;
    private Long softwareModuleId;
    private String sha1Hash;

    @Before
    public void setup() {
        final DistributionSet dsA = TestDataUtil.generateDistributionSet("", softwareManagement,
                distributionSetManagement);
        final SoftwareModule module = dsA.getModules().iterator().next();
        localArtifact = (LocalArtifact) TestDataUtil.generateArtifacts(artifactManagement, module.getId()).stream()
                .findAny().get();
        softwareModuleId = localArtifact.getSoftwareModule().getId();
        fileName = localArtifact.getFilename();
        sha1Hash = localArtifact.getSha1Hash();

    }

    @Test
    @Description("Tests the generation of http download url.")
    public void testHttpUrl() {

        final String url = urlHandlerProperties.getUrl(controllerId, softwareModuleId, fileName, sha1Hash,
                UrlProtocol.HTTP);
        assertEquals("http is build incorrect",
                "http://localhost:8080/" + tenantAware.getCurrentTenant() + "/controller/v1/" + controllerId
                        + "/softwaremodules/" + localArtifact.getSoftwareModule().getId() + "/artifacts/"
                        + localArtifact.getFilename(),
                url);
    }

    @Test
    @Description("Tests the generation of https download url.")
    public void testHttpsUrl() {
        final String url = urlHandlerProperties.getUrl(controllerId, softwareModuleId, fileName, sha1Hash,
                UrlProtocol.HTTPS);
        assertEquals("https is build incorrect",
                "https://localhost:8080/" + tenantAware.getCurrentTenant() + "/controller/v1/" + controllerId
                        + "/softwaremodules/" + localArtifact.getSoftwareModule().getId() + "/artifacts/"
                        + localArtifact.getFilename(),
                url);
    }

    @Test
    @Description("Tests the generation of coap download url.")
    public void testCoapUrl() {
        final String url = urlHandlerProperties.getUrl(controllerId, softwareModuleId, fileName, sha1Hash,
                UrlProtocol.COAP);

        assertEquals("coap is build incorrect", "coap://127.0.0.1:5683/fw/" + tenantAware.getCurrentTenant() + "/"
                + controllerId + "/sha1/" + localArtifact.getSha1Hash(), url);
    }
}
