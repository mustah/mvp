package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.PeriodFilter;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.tables.MissingMeasurement.MISSING_MEASUREMENT;
import static com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeter.PHYSICAL_METER;
import static org.jooq.impl.DSL.falseCondition;

public class MissingMeasurementJooqConditions extends EmptyJooqFilterVisitor {

  private Condition condition = falseCondition();

  @Override
  public void visit(PeriodFilter periodFilter) {
    var period = periodFilter.getPeriod();

    condition = MISSING_MEASUREMENT.EXPECTED_TIME.greaterOrEqual(period.start.toOffsetDateTime())
      .and(MISSING_MEASUREMENT.EXPECTED_TIME.lessThan(period.stop.toOffsetDateTime()));
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> applyJoins(SelectJoinStep<R> query) {
    return query.leftJoin(MISSING_MEASUREMENT)
      .on(MISSING_MEASUREMENT.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID).and(condition));
  }
}
