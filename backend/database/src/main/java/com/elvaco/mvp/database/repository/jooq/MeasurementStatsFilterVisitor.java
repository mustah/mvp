package com.elvaco.mvp.database.repository.jooq;

import java.sql.Date;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.FilterPeriod;
import com.elvaco.mvp.core.domainmodels.MeasurementThreshold;
import com.elvaco.mvp.core.filter.MeasurementThresholdFilter;
import com.elvaco.mvp.core.filter.ThresholdPeriodFilter;
import com.elvaco.mvp.core.util.MeasurementThresholdParser;
import com.elvaco.mvp.database.util.DurationLongerThanSelectionException;

import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;

import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT_STAT_DATA;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.measurementStatsConditionFor;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.valueConditionFor;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.min;
import static org.jooq.impl.DSL.partitionBy;
import static org.jooq.impl.DSL.rowNumber;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectOne;

class MeasurementStatsFilterVisitor extends EmptyFilterVisitor {

  private final MeasurementThresholdParser thresholdParser;

  private Condition measurementStatsCondition = falseCondition();
  private Condition measurementStatsFilter = falseCondition();
  private Condition measurementThresholdDuringFilter;
  private Long measurementThresholdDuringDuration;
  private Long selectionDurationInDays;

  MeasurementStatsFilterVisitor(MeasurementThresholdParser thresholdParser) {
    super();
    this.thresholdParser = thresholdParser;
  }

  @Override
  public void visit(ThresholdPeriodFilter filter) {
    FilterPeriod period = filter.getPeriod();
    selectionDurationInDays = ChronoUnit.DAYS.between(period.start, period.stop);
    measurementStatsCondition = measurementStatsConditionFor(period);
  }

  @Override
  public void visit(MeasurementThresholdFilter filter) {
    MeasurementThreshold threshold = thresholdParser.parse(filter.oneValue());
    if (threshold.duration != null) {
      measurementThresholdDuringDuration = threshold.duration.toDays();
      measurementThresholdDuringFilter =
        MEASUREMENT_STAT_DATA.QUANTITY.equal(threshold.quantity.getId())
          .and(MEASUREMENT_STAT_DATA.IS_CONSUMPTION.equal(
            threshold.quantity.isConsumptionSeries()))
          .and(valueConditionFor(threshold));
    } else {
      measurementStatsFilter =
        MEASUREMENT_STAT_DATA.QUANTITY.equal(threshold.quantity.getId())
          .and(MEASUREMENT_STAT_DATA.IS_CONSUMPTION.equal(
            threshold.quantity.isConsumptionSeries()))
          .and(valueConditionFor(threshold));
    }
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    if (!measurementStatsFilter.equals(falseCondition())
      && !measurementStatsCondition.equals(falseCondition())) {
      addCondition(exists(selectOne().from(MEASUREMENT_STAT_DATA)
        .where(measurementStatsFilter.and(measurementStatsCondition))));
    }

    if (measurementThresholdDuringFilter != null
      && !measurementStatsCondition.equals(falseCondition())
      && measurementThresholdDuringDuration != null) {
      if (selectionDurationInDays != null
        && measurementThresholdDuringDuration > selectionDurationInDays) {
        throw new DurationLongerThanSelectionException();
      }

      Table<Record3<UUID, Date, Date>> periodGroups = select(
        MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID,
        MEASUREMENT_STAT_DATA.STAT_DATE,
        MEASUREMENT_STAT_DATA.STAT_DATE.minus(
          rowNumber().over(
            partitionBy(MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID)
              .orderBy(MEASUREMENT_STAT_DATA.STAT_DATE)
          ).concat(" days").cast(SQLDataType.INTERVALDAYTOSECOND)).as("period_group")
      ).from(MEASUREMENT_STAT_DATA)
        .where(measurementThresholdDuringFilter)
        .and(measurementStatsCondition)
        .asTable("period_groups");

      addCondition(exists(
        selectOne()
          .from(periodGroups)
          .groupBy(
            periodGroups.field("physical_meter_id"),
            periodGroups.field("period_group")
          ).having(max(periodGroups.field("stat_date")).minus(
          min(periodGroups.field("stat_date")))
          .cast(Long.class).plus(1).greaterOrEqual(measurementThresholdDuringDuration))
      ));
    }
    return query;
  }
}
