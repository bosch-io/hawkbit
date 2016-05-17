/**
 * Copyright (c) 2011-2016 Bosch Software Innovations GmbH, Germany. All rights reserved.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ddi.dl.rest.api;

import java.io.InputStream;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * REST resource handling for artifact download operations.
 */
@RequestMapping(DdiDlRestConstants.ARTIFACTS_V1_REQUEST_MAPPING)
public interface DdiDlArtifactStoreControllerRestApi {

    /**
     * Handles GET download request. This could be full or partial download
     * request.
     *
     * @param fileName
     *            to search for
     * @param targetid
     *            of authenticated target
     *
     * @return response of the servlet which in case of success is status code
     *         {@link HttpStatus#OK} or in case of partial download
     *         {@link HttpStatus#PARTIAL_CONTENT}.
     */
    @RequestMapping(method = RequestMethod.GET, value = DdiDlRestConstants.ARTIFACT_DOWNLOAD_BY_FILENAME + "/{fileName}")
    @ResponseBody
    public ResponseEntity<InputStream> downloadArtifactByFilename(@PathVariable("fileName") final String fileName,
            @AuthenticationPrincipal final String targetid);

    /**
     * Handles GET MD5 checksum file download request.
     *
     * @param fileName
     *            to search for
     *
     * @return response of the servlet
     */
    @RequestMapping(method = RequestMethod.GET, value = DdiDlRestConstants.ARTIFACT_DOWNLOAD_BY_FILENAME + "/{fileName}"
            + DdiDlRestConstants.ARTIFACT_MD5_DWNL_SUFFIX)
    @ResponseBody
    public ResponseEntity<Void> downloadArtifactMD5ByFilename(@PathVariable("fileName") final String fileName);

}
