package com.elvaco.mvp.database.repository.jooq;

import java.time.OffsetDateTime;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.filter.PeriodFilter;

import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.Table;

import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT_STAT_DATA;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.measurementStatsConditionFor;
import static org.jooq.impl.DSL.cast;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.min;
import static org.jooq.impl.DSL.nullif;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.sum;
import static org.jooq.impl.DSL.trueCondition;
import static org.jooq.impl.DSL.when;

@RequiredArgsConstructor
public class CollectionPercentagePerDateFilterVisitor extends EmptyFilterVisitor {

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
      throw new RuntimeException("No period selected");
    } else {
      Table series = JooqUtils.dateSerieFor(
        period.start.toOffsetDateTime(),
        period.stop.toOffsetDateTime().minusDays(1L),
        "1 days",
        false,
        "gendate"
      );

      condition = measurementStatsConditionFor(period)
        .and(MEASUREMENT_STAT_DATA.IS_CONSUMPTION.isFalse())
        .and(MEASUREMENT_STAT_DATA.QUANTITY.equal(dsl.select(MEASUREMENT_STAT_DATA.QUANTITY)
          .from(MEASUREMENT_STAT_DATA)
          .where(MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
          .limit(1)))
        .and(JooqUtils.periodOverlaps(PHYSICAL_METER.ACTIVE_PERIOD, period.toPeriodRange()))
        .and(PHYSICAL_METER.LOGICAL_METER_ID.eq(LOGICAL_METER.ID));

      return query.leftJoin(lateral(
        dsl.select(
          sum(when(
            PHYSICAL_METER.READ_INTERVAL_MINUTES.ne(0L),
            coalesce(field("actual", Long.class), 0L)
          )
            .otherwise(inline((Long) null))).as("actual"),
          inline(1440L) //60*24
            .divide(nullif(PHYSICAL_METER.READ_INTERVAL_MINUTES, 0L)).as("expected"),
          field("gendate")

        ).from(select(cast(field("gendate"),OffsetDateTime.class).as("gendate")).from(series)
          .asTable("series")
          .leftJoin(
          dsl.select(
            when(
              min(PHYSICAL_METER.READ_INTERVAL_MINUTES).ne(0L),
              coalesce(
                sum(
                  coalesce(MEASUREMENT_STAT_DATA.RECEIVED_COUNT, 0)
                ),
                0L
              )
            )
              .otherwise(inline((Long) null)).as("actual"),
            MEASUREMENT_STAT_DATA.STAT_DATE
          )
            .from(PHYSICAL_METER)
            .leftJoin(MEASUREMENT_STAT_DATA)
            .on(MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
            .where(condition)
            .groupBy(
              MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID,
              MEASUREMENT_STAT_DATA.STAT_DATE
            ).asTable("meter_stats"))
          .on("series.gendate=stat_date")).groupBy(field("gendate"))
          .asTable("foo"))).on(trueCondition());


    }
  }
}
