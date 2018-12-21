package com.elvaco.mvp.database.repository.jooq;

import java.time.OffsetDateTime;

import com.elvaco.mvp.core.filter.PeriodFilter;

import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeterStatusLog.PHYSICAL_METER_STATUS_LOG;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.max;

@RequiredArgsConstructor
class PhysicalMeterStatusLogFilterVisitor extends EmptyFilterVisitor {

  private final DSLContext dsl;

  private Condition condition = falseCondition();

  @Override
  public void visit(PeriodFilter filter) {
    OffsetDateTime stop = filter.getPeriod().stop.toOffsetDateTime();

    condition = PHYSICAL_METER_STATUS_LOG.START.lessThan(stop)
      .and(PHYSICAL_METER_STATUS_LOG.STOP.isNull()
        .or(PHYSICAL_METER_STATUS_LOG.STOP.greaterOrEqual(stop)));
  }

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
                .and(condition)))
        )));
  }
}
