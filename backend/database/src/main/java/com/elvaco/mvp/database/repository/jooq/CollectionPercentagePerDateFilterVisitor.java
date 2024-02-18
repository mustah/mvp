package com.elvaco.mvp.database.repository.jooq;

import java.time.LocalDate;

import com.elvaco.mvp.core.domainmodels.FilterPeriod;
import com.elvaco.mvp.core.filter.CollectionPeriodFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;

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

  private static final Field<LocalDate> GENDATE_FIELD = field("gendate", LocalDate.class);

  private static final String ACTUAL = "actual";

  private final DSLContext dsl;

  private FilterPeriod period;

  private Condition orgCondition = trueCondition();

  @Override
  public void visit(CollectionPeriodFilter filter) {
    period = filter.getPeriod();
  }

  @Override
  public void visit(OrganisationIdFilter filter) {
    orgCondition = MEASUREMENT_STAT_DATA.ORGANISATION_ID.in(filter.values());
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    Condition condition;
    if (period == null) {
      throw new NullPointerException("No period selected");
    } else {
      var series = JooqUtils.dateSerieFor(
        period.start.toLocalDate(),
        period.stop.toLocalDate().minusDays(1L),
        "1 days",
        GENDATE_FIELD.getName()
      );

      condition = measurementStatsConditionFor(period)
        .and(MEASUREMENT_STAT_DATA.IS_CONSUMPTION.isFalse())
        .and(MEASUREMENT_STAT_DATA.QUANTITY_ID.equal(
          dsl.select(MEASUREMENT_STAT_DATA.QUANTITY_ID)
            .from(MEASUREMENT_STAT_DATA)
            .where(MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
            .and(MEASUREMENT_STAT_DATA.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID))
            .and(orgCondition)
            .and(MEASUREMENT_STAT_DATA.IS_CONSUMPTION.isFalse())
            .orderBy(MEASUREMENT_STAT_DATA.STAT_DATE.desc())
            .limit(1)
          )
        )
        .and(JooqUtils.periodOverlaps(PHYSICAL_METER.ACTIVE_PERIOD, period.toPeriodRange()))
        .and(PHYSICAL_METER.LOGICAL_METER_ID.eq(LOGICAL_METER.ID));

      return query.leftJoin(lateral(
        dsl.select(
          sum(when(
            PHYSICAL_METER.READ_INTERVAL_MINUTES.ne(0L),
            coalesce(field(ACTUAL, Long.class), 0L)
          )
            .otherwise(inline((Long) null))).as(ACTUAL),
          inline(1440L) //60*24
            .divide(nullif(PHYSICAL_METER.READ_INTERVAL_MINUTES, 0L)).as("expected"),
          GENDATE_FIELD

        ).from(
          select(GENDATE_FIELD)
            .from(series)
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
                  .otherwise(inline((Long) null)).as(ACTUAL),
                MEASUREMENT_STAT_DATA.STAT_DATE
              )
                .from(PHYSICAL_METER)
                .leftJoin(MEASUREMENT_STAT_DATA)
                .on(MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
                  .and(MEASUREMENT_STAT_DATA.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)))
                .where(condition)
                .groupBy(
                  MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID,
                  MEASUREMENT_STAT_DATA.STAT_DATE
                ).asTable("meter_stats"))
            .on("series.gendate=stat_date")).groupBy(GENDATE_FIELD)
          .asTable("foo"))).on(trueCondition());
    }
  }
}
