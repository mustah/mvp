package com.elvaco.mvp.database.repository.jooq;

import org.jooq.Record;
import org.jooq.SelectJoinStep;

@FunctionalInterface
public interface Joins {

  <R extends Record> Joins andJoinsOn(SelectJoinStep<R> query);
}
