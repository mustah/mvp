package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.OrganisationIdFilter;

import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.LAST_DATA;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.trueCondition;

@RequiredArgsConstructor
public class MeasurementLastDataFilterVisitor extends EmptyFilterVisitor {
  private final DSLContext dsl;

  private Condition orgCondition = falseCondition();

  @Override
  public void visit(OrganisationIdFilter filter) {
    orgCondition = MEASUREMENT.ORGANISATION_ID.in(filter.values());
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query.leftJoin(

      lateral(
        dsl.select(max(MEASUREMENT.READOUT_TIME).as(LAST_DATA))
          .from(MEASUREMENT)
          .where(MEASUREMENT.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
          .and(MEASUREMENT.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID))
        .and(orgCondition)
      ).as("last_measurement")
    ).on(trueCondition());
  }
}
