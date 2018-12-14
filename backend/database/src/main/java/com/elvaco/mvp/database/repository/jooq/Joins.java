package com.elvaco.mvp.database.repository.jooq;

import org.jooq.Record;
import org.jooq.SelectJoinStep;

public interface Joins {

  <R extends Record> Joins applyJoinsOn(SelectJoinStep<R> query);
}
