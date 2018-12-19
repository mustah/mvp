package com.elvaco.mvp.database.repository.jooq;

import java.time.OffsetDateTime;

import com.elvaco.mvp.core.filter.PeriodFilter;

import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.METER_ALARM_LOG;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.trueCondition;

@RequiredArgsConstructor
class MeterAlarmLogFilterVisitor extends EmptyFilterVisitor {

  private final DSLContext dsl;

  private Condition condition = falseCondition();

  @Override
  public void visit(PeriodFilter filter) {
    OffsetDateTime stop = filter.getPeriod().stop.toOffsetDateTime();

    condition = METER_ALARM_LOG.START.lessThan(stop)
      .and(METER_ALARM_LOG.STOP.isNull()
        .or(METER_ALARM_LOG.STOP.greaterOrEqual(stop)));
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query.leftJoin(lateral(
      dsl.select()
        .from(METER_ALARM_LOG)
        .where(METER_ALARM_LOG.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
          .and(METER_ALARM_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
          .and(condition))
        .orderBy(METER_ALARM_LOG.START.desc())
        .limit(1)
        .asTable(METER_ALARM_LOG.getName())
    )).on(trueCondition());
  }
}
