package com.elvaco.mvp.database.repository.jooq;

import java.util.function.Function;

import com.elvaco.mvp.core.filter.Filters;

import org.jooq.Record;
import org.jooq.SelectJoinStep;

@FunctionalInterface
public interface FilterAcceptor {

  <R extends Record> Function<SelectJoinStep<? extends R>, Joins> accept(Filters filters);
}
