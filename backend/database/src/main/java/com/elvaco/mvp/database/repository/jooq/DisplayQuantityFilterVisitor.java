package com.elvaco.mvp.database.repository.jooq;

import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.DISPLAY_QUANTITY;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.Tables.QUANTITY;

class DisplayQuantityFilterVisitor extends EmptyFilterVisitor {

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query
      .join(DISPLAY_QUANTITY)
      .on(METER_DEFINITION.ID.eq(DISPLAY_QUANTITY.METER_DEFINITION_ID))

      .leftJoin(QUANTITY)
      .on(QUANTITY.ID.eq(DISPLAY_QUANTITY.QUANTITY_ID));
  }
}
