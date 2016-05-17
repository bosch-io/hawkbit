/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * An implementation of the {@link PageRequest} which is offset based by means
 * the offset is given and not the page number as in the original
 * {@link PageRequest} implemntation where the offset is generated. Due that the
 * REST-API is working with {@code offset} and {@code limit} parameter we need
 * an offset based page request for JPA.
 * 
 *
 *
 *
 */
public final class OffsetBasedPageRequest extends PageRequest {

    private static final long serialVersionUID = 1L;
    private final int offset;

    /**
     * Creates a new {@link OffsetBasedPageRequest}. Offsets are zero indexed,
     * thus providing 0 for {@code offset} will return the first entry.
     * 
     * @param offset
     *            zero-based offset index.
     * @param limit
     *            the limit of the page to be returned.
     */
    public OffsetBasedPageRequest(final int offset, final int limit) {
        this(offset, limit, null);
    }

    /**
     * Creates a new {@link OffsetBasedPageRequest}. Offsets are zero indexed,
     * thus providing 0 for {@code offset} will return the first entry.
     * 
     * @param offset
     *            zero-based offset index.
     * @param limit
     *            the limit of the page to be returned.
     * @param sort
     *            sort can be {@literal null}.
     */
    public OffsetBasedPageRequest(final int offset, final int limit, final Sort sort) {
        super(0, limit, sort);
        this.offset = offset;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "OffsetBasedPageRequest [offset=" + offset + ", getPageSize()=" + getPageSize() + ", getPageNumber()="
                + getPageNumber() + "]";
    }
}
