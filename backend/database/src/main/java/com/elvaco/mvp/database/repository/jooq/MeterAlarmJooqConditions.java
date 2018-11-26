package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.PeriodFilter;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.METER_ALARM_LOG;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.max;

@RequiredArgsConstructor
public class MeterAlarmJooqConditions extends EmptyJooqFilterVisitor {

  private final DSLContext dsl;

  private Condition meterAlarmLogCondition = falseCondition();

  @Override
  public void visit(PeriodFilter periodFilter) {
    var period = periodFilter.getPeriod();

    meterAlarmLogCondition = METER_ALARM_LOG.START.between(
      period.start.toOffsetDateTime(),
      period.stop.toOffsetDateTime()
    ).or(METER_ALARM_LOG.STOP.isNull());
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> applyJoins(SelectJoinStep<R> query) {
    return query.leftJoin(METER_ALARM_LOG)
      .on(METER_ALARM_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
        .and(METER_ALARM_LOG.ID.equal(dsl
          .select(max(METER_ALARM_LOG.ID))
          .from(METER_ALARM_LOG)
          .where(METER_ALARM_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
            .and(meterAlarmLogCondition))))
        .or(METER_ALARM_LOG.ID.isNull()));
  }
}
