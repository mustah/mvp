package com.elvaco.mvp.database.repository.jooq;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeterStatusLog.PHYSICAL_METER_STATUS_LOG;
import static org.jooq.impl.DSL.max;

@RequiredArgsConstructor
class PhysicalMeterStatusLogFilterVisitor extends EmptyFilterVisitor {

  private final DSLContext dsl;

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query.leftJoin(PHYSICAL_METER_STATUS_LOG)
      .on(PHYSICAL_METER_STATUS_LOG.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
        .and(PHYSICAL_METER_STATUS_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
        .and(PHYSICAL_METER_STATUS_LOG.ID.equal(
          dsl.select(max(PHYSICAL_METER_STATUS_LOG.ID))
            .from(PHYSICAL_METER_STATUS_LOG)
            .where(PHYSICAL_METER_STATUS_LOG.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
              .and(PHYSICAL_METER_STATUS_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
                .and(PHYSICAL_METER_STATUS_LOG.STOP.isNull())))
        )));
  }
}
