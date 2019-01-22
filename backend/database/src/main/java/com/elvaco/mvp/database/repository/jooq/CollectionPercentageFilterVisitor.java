package com.elvaco.mvp.database.repository.jooq;

import java.time.temporal.ChronoUnit;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.filter.PeriodFilter;

import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT_STAT_DATA;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.COLLECTION_PERCENTAGE;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.METER_STATS;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.measurementStatsConditionFor;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.min;
import static org.jooq.impl.DSL.nullif;
import static org.jooq.impl.DSL.sum;
import static org.jooq.impl.DSL.trueCondition;

@RequiredArgsConstructor
class CollectionPercentageFilterVisitor extends EmptyFilterVisitor {

  private final DSLContext dsl;

  private SelectionPeriod period;

  @Override
  public void visit(PeriodFilter filter) {
    period = filter.getPeriod();
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    Condition condition;
    Field<Double> collectionPercentageField;
    if (period == null) {
      condition = falseCondition();
      collectionPercentageField = inline(0.0);
    } else {
      condition = measurementStatsConditionFor(period)
        .and(MEASUREMENT_STAT_DATA.QUANTITY.equal(dsl.select(MEASUREMENT_STAT_DATA.QUANTITY)
          .from(MEASUREMENT_STAT_DATA)
          .where(MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
          .limit(1)))
        .and(JooqUtils.periodOverlaps(PHYSICAL_METER.ACTIVE_PERIOD, period.toPeriodRange()))
        .and(PHYSICAL_METER.LOGICAL_METER_ID.eq(LOGICAL_METER.ID));

      collectionPercentageField = sum(coalesce(MEASUREMENT_STAT_DATA.RECEIVED_COUNT, 0))
        .divide(
          inline(60, Double.class)
            .times(ChronoUnit.HOURS.between(period.start, period.stop))
            .divide(min(nullif(PHYSICAL_METER.READ_INTERVAL_MINUTES, 0L)))
        ).times(100.0).cast(Double.class);
    }

    return query.leftJoin(
      lateral(
        dsl.select(collectionPercentageField.as(COLLECTION_PERCENTAGE))
          .from(PHYSICAL_METER)
          .leftJoin(MEASUREMENT_STAT_DATA)
          .on(MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
          .where(condition)
      ).as(METER_STATS)
    ).on(trueCondition());
  }
}
