/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.providers;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.hawkbit.ui.common.data.mappers.IdentifiableEntityToProxyIdentifiableEntityMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.hateoas.Identifiable;

import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;

/**
 * Base class for loading a batch of {@link Identifiable} entities from backend
 * mapping them to {@link ProxyIdentifiableEntity} entities.
 */
public abstract class ProxyDataProvider<T extends ProxyIdentifiableEntity, U extends Identifiable<Long>, F>
        extends AbstractBackEndDataProvider<T, F> {

    private static final long serialVersionUID = 1L;

    private final Sort defaultSortOrder = new Sort(Direction.ASC, "id");

    private final IdentifiableEntityToProxyIdentifiableEntityMapper<T, U> entityMapper;

    public ProxyDataProvider(final IdentifiableEntityToProxyIdentifiableEntityMapper<T, U> mapper) {
        this.entityMapper = mapper;
    }

    @Override
    protected Stream<T> fetchFromBackEnd(final Query<T, F> query) {
        final int pagesize = query.getLimit() > 0 ? query.getLimit() : SPUIDefinitions.PAGE_SIZE;
        final PageRequest pageRequest = PageRequest.of(query.getOffset() / pagesize, pagesize, defaultSortOrder);
        return getProxyRolloutList(loadBeans(pageRequest, query.getFilter().orElse(null))).stream();
    }

    private List<T> getProxyRolloutList(final Optional<Slice<U>> rolloutBeans) {
        return rolloutBeans
                .map(beans -> beans.getContent().stream().map(entityMapper::map).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    protected abstract Optional<Slice<U>> loadBeans(final PageRequest pageRequest, F filter);

    @Override
    protected int sizeInBackEnd(final Query<T, F> query) {
        final int pagesize = query.getLimit() > 0 ? query.getLimit() : SPUIDefinitions.PAGE_SIZE;
        final PageRequest pageRequest = PageRequest.of(query.getOffset() / pagesize, pagesize, defaultSortOrder);

        final long size = sizeInBackEnd(pageRequest, query.getFilter().orElse(null));

        if (size > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) size;
    }

    protected abstract long sizeInBackEnd(final PageRequest pageRequest, F filter);

    @Override
    public Object getId(final T item) {
        Objects.requireNonNull(item, "Cannot provide an id for a null item.");
        return item.getId();
    }
}
