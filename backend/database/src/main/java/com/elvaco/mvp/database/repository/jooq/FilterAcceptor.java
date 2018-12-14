package com.elvaco.mvp.database.repository.jooq;

import java.util.function.Function;

import com.elvaco.mvp.core.filter.Filters;

public interface FilterAcceptor extends Function<Filters, Joins> {

}
