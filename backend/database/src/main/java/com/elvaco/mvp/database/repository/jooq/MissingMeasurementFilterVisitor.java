package com.elvaco.mvp.database.repository.jooq;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.filter.PeriodFilter;

import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.MISSING_MEASUREMENT;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.MISSING_MEASUREMENT_COUNT;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.lateral;

@RequiredArgsConstructor
class MissingMeasurementFilterVisitor extends EmptyFilterVisitor {

  private static final String TABLE_ALIAS = "mm";
  private static final Field<UUID> MISSING_MEASUREMENT_PHYSICAL_METER_ID_FIELD =
    field(TABLE_ALIAS + ".physical_meter_id", UUID.class);

  private final DSLContext dsl;

  private Condition condition = falseCondition();

  @Override
  public void visit(PeriodFilter filter) {
    SelectionPeriod period = filter.getPeriod();
    condition = MISSING_MEASUREMENT.EXPECTED_TIME.greaterOrEqual(period.start.toOffsetDateTime())
      .and(MISSING_MEASUREMENT.EXPECTED_TIME.lessThan(period.stop.toOffsetDateTime()));
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    return query.leftJoin(lateral(
      dsl.select(count().as(MISSING_MEASUREMENT_COUNT), MISSING_MEASUREMENT.PHYSICAL_METER_ID)
        .from(MISSING_MEASUREMENT)
        .where(MISSING_MEASUREMENT.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
          .and(condition))
        .groupBy(MISSING_MEASUREMENT.PHYSICAL_METER_ID)).asTable(TABLE_ALIAS))
      .on(PHYSICAL_METER.ID.equal(MISSING_MEASUREMENT_PHYSICAL_METER_ID_FIELD));
  }
}
