package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.Filters;

@FunctionalInterface
public interface FilterAcceptor {

  Joins accept(Filters filters);
}
