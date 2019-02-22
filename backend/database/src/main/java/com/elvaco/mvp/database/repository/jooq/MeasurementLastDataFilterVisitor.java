package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.PeriodFilter;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.LAST_DATA;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.trueCondition;

@RequiredArgsConstructor
public class MeasurementLastDataFilterVisitor extends EmptyFilterVisitor {
  private final DSLContext dsl;

  @Override
  public void visit(PeriodFilter filter) { }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query.leftJoin(

      lateral(
        dsl.select(max(MEASUREMENT.CREATED).as(LAST_DATA))
          .from(MEASUREMENT)
          .where(MEASUREMENT.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
      ).as("last_measurement")
    ).on(trueCondition());
  }
}
