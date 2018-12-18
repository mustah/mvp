package com.elvaco.mvp.database.repository.jooq;

import java.sql.Date;
import java.time.LocalDate;

import com.elvaco.mvp.core.domainmodels.MeasurementThreshold;
import com.elvaco.mvp.core.filter.MeasurementThresholdFilter;
import com.elvaco.mvp.core.filter.PeriodFilter;
import com.elvaco.mvp.core.util.MeasurementThresholdParser;

import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT_STAT_DATA;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.valueConditionFor;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.select;

class MeasurementStatsFilterVisitor extends EmptyFilterVisitor {

  private final MeasurementThresholdParser thresholdParser;

  private Condition measurementStatsCondition = falseCondition();
  private Condition measurementStatsFilter = falseCondition();

  MeasurementStatsFilterVisitor(MeasurementThresholdParser thresholdParser) {
    super();
    this.thresholdParser = thresholdParser;
  }

  @Override
  public void visit(PeriodFilter filter) {
    var period = filter.getPeriod();

    LocalDate startDate = period.start.toLocalDate();
    LocalDate stopDate = period.stop.toLocalDate();
    if (stopDate.isEqual(startDate)) {
      measurementStatsCondition =
        MEASUREMENT_STAT_DATA.STAT_DATE.equal(Date.valueOf(startDate))
          .and(MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID));
    } else {
      measurementStatsCondition =
        MEASUREMENT_STAT_DATA.STAT_DATE.greaterOrEqual(Date.valueOf(startDate))
          .and(MEASUREMENT_STAT_DATA.STAT_DATE.lessThan(Date.valueOf(stopDate)))
          .and(MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID));
    }
  }

  @Override
  public void visit(MeasurementThresholdFilter filter) {
    MeasurementThreshold threshold = thresholdParser.parse(filter.oneValue());

    measurementStatsFilter = MEASUREMENT_STAT_DATA.QUANTITY.equal(threshold.quantity.getId())
      .and(valueConditionFor(threshold));
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> joinOn(SelectJoinStep<R> query) {
    if (!measurementStatsFilter.equals(falseCondition())
      && !measurementStatsCondition.equals(falseCondition())) {
      addCondition(exists(select().from(MEASUREMENT_STAT_DATA)
        .where(measurementStatsFilter.and(measurementStatsCondition))));
    }
    return query;
  }
}
