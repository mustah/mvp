package com.elvaco.mvp.database.repository.jooq;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.METER_ALARM_LOG;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static org.jooq.impl.DSL.max;

@RequiredArgsConstructor
class MeterAlarmLogFilterVisitor extends EmptyFilterVisitor {

  private final DSLContext dsl;

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query.leftJoin(METER_ALARM_LOG)
      .on(METER_ALARM_LOG.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
        .and(METER_ALARM_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
        .and(METER_ALARM_LOG.ID.equal(
          dsl.select(max(METER_ALARM_LOG.ID))
            .from(METER_ALARM_LOG)
            .where(METER_ALARM_LOG.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
              .and(METER_ALARM_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
                .and(METER_ALARM_LOG.STOP.isNull())))
        )));
  }
}
