package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.PeriodFilter;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.METER_ALARM_LOG;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;

public class MeterAlarmJooqPredicates extends EmptyJooqFilterVisitor {

  @Override
  public void visit(PeriodFilter periodFilter) {
    var period = periodFilter.getPeriod();

    var withinRange = METER_ALARM_LOG.START.between(
      period.start.toOffsetDateTime(),
      period.stop.toOffsetDateTime()
    ).or(METER_ALARM_LOG.STOP.isNull());

    addCondition(withinRange);
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> applyJoins(SelectJoinStep<R> query) {
    return query.leftJoin(METER_ALARM_LOG)
      .on(METER_ALARM_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID));
  }
}
