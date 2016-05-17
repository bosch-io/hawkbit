/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ddi.client.resource;

import org.eclipse.hawkbit.ddi.rest.api.DdiRootControllerRestApi;
import org.springframework.cloud.netflix.feign.FeignClient;

/**
 * Client binding for the Rootcontroller resource of the DDI API.
 */
@FeignClient(url = "${hawkbit.url:localhost:8080}/" + RootControllerResourceClientConstants.PATH)
public interface RootControllerResourceClient extends DdiRootControllerRestApi {

}
