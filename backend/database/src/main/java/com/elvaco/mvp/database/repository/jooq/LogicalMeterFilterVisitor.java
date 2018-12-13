package com.elvaco.mvp.database.repository.jooq;

import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.MeasurementThreshold;
import com.elvaco.mvp.core.filter.MeasurementThresholdFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.PeriodFilter;
import com.elvaco.mvp.core.filter.WildcardFilter;
import com.elvaco.mvp.core.util.MeasurementThresholdParser;

import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.GATEWAY;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.MEASUREMENT_STAT_DATA;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_ALARM_LOG;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.Tables.MISSING_MEASUREMENT;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.GatewaysMeters.GATEWAYS_METERS;
import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeterStatusLog.PHYSICAL_METER_STATUS_LOG;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.exists;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.select;

@RequiredArgsConstructor
public class LogicalMeterFilterVisitor extends CommonFilterVisitor {

  public static final Field<Long> MISSING_MEASUREMENT_COUNT = field(
    "missing_measurement_count",
    Long.class
  );

  private final DSLContext dsl;
  private final MeasurementThresholdParser measurementThresholdParser;

  private Condition physicalMeterStatusLogCondition = falseCondition();
  private Condition meterAlarmLogCondition = falseCondition();
  private Condition missingMeasurementCondition = falseCondition();
  private Condition measurementStatsCondition = falseCondition();
  private Condition measurementStatsFilter = falseCondition();

  @Override
  public void visit(OrganisationIdFilter filter) {
    addCondition(LOGICAL_METER.ORGANISATION_ID.in(filter.values()));
  }

  @Override
  public void visit(WildcardFilter filter) {
    var value = filter.oneValue().toLowerCase();
    addCondition(LOGICAL_METER.EXTERNAL_ID.lower().startsWith(value)
      .or(METER_DEFINITION.MEDIUM.lower().startsWith(value))
      .or(LOCATION.CITY.lower().startsWith(value))
      .or(LOCATION.STREET_ADDRESS.lower().startsWith(value))
      .or(PHYSICAL_METER.MANUFACTURER.lower().startsWith(value))
      .or(PHYSICAL_METER.ADDRESS.lower().startsWith(value)));
  }

  @Override
  public void visit(PeriodFilter filter) {
    var period = filter.getPeriod();

    missingMeasurementCondition =
      MISSING_MEASUREMENT.EXPECTED_TIME.greaterOrEqual(period.start.toOffsetDateTime())
        .and(MISSING_MEASUREMENT.EXPECTED_TIME.lessThan(period.stop.toOffsetDateTime()));

    physicalMeterStatusLogCondition =
      PHYSICAL_METER_STATUS_LOG.START.lessThan(period.stop.toOffsetDateTime())
        .and(PHYSICAL_METER_STATUS_LOG.STOP.isNull()
          .or(PHYSICAL_METER_STATUS_LOG.STOP.greaterOrEqual(period.start.toOffsetDateTime())));

    meterAlarmLogCondition = METER_ALARM_LOG.START.between(
      period.start.toOffsetDateTime(),
      period.stop.toOffsetDateTime()
    ).or(METER_ALARM_LOG.STOP.isNull());

    LocalDate startDate = period.start.toLocalDate();
    LocalDate stopDate = period.stop.toLocalDate();
    if (stopDate.isEqual(startDate)) {
      measurementStatsCondition = MEASUREMENT_STAT_DATA.STAT_DATE.equal(Date.valueOf(startDate))
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
    MeasurementThreshold threshold = measurementThresholdParser.parse(filter.oneValue());

    measurementStatsFilter = MEASUREMENT_STAT_DATA.QUANTITY.equal(threshold.quantity.getId())
      .and(valueConditionFor(threshold));
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> applyJoins(SelectJoinStep<R> query) {
    if (!measurementStatsFilter.equals(falseCondition())
      && !measurementStatsCondition.equals(falseCondition())) {
      addCondition(exists(select()
        .from(MEASUREMENT_STAT_DATA)
        .where(measurementStatsFilter.and(measurementStatsCondition))));
    }

    return query.leftJoin(PHYSICAL_METER)
      .on(PHYSICAL_METER.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
        .and(PHYSICAL_METER.LOGICAL_METER_ID.equal(LOGICAL_METER.ID))
      )

      .leftJoin(GATEWAYS_METERS)
      .on(GATEWAYS_METERS.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
        .and(GATEWAYS_METERS.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)))
      .leftJoin(GATEWAY)
      .on(GATEWAY.ORGANISATION_ID.equal(GATEWAYS_METERS.ORGANISATION_ID)
        .and(GATEWAY.ID.equal(GATEWAYS_METERS.GATEWAY_ID)))

      .leftJoin(METER_DEFINITION)
      .on(METER_DEFINITION.TYPE.equal(LOGICAL_METER.METER_DEFINITION_TYPE))

      .leftJoin(LOCATION)
      .on(LOCATION.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
        .and(LOCATION.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)))

      .leftJoin(PHYSICAL_METER_STATUS_LOG)
      .on(PHYSICAL_METER_STATUS_LOG.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
        .and(PHYSICAL_METER_STATUS_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
        .and(PHYSICAL_METER_STATUS_LOG.ID.equal(dsl
          .select(max(PHYSICAL_METER_STATUS_LOG.ID))
          .from(PHYSICAL_METER_STATUS_LOG)
          .where(PHYSICAL_METER_STATUS_LOG.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
            .and(PHYSICAL_METER_STATUS_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
              .and(physicalMeterStatusLogCondition))))))

      .leftJoin(METER_ALARM_LOG)
      .on(METER_ALARM_LOG.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
        .and(METER_ALARM_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID))
        .and(METER_ALARM_LOG.ID.equal(dsl
          .select(max(METER_ALARM_LOG.ID))
          .from(METER_ALARM_LOG)
          .where(METER_ALARM_LOG.ORGANISATION_ID.equal(PHYSICAL_METER.ORGANISATION_ID)
            .and(METER_ALARM_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
              .and(meterAlarmLogCondition))))))

      .leftJoin(lateral(dsl
        .select(count().as(MISSING_MEASUREMENT_COUNT), MISSING_MEASUREMENT.PHYSICAL_METER_ID)
        .from(MISSING_MEASUREMENT)
        .where(MISSING_MEASUREMENT.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
          .and(missingMeasurementCondition))
        .groupBy(MISSING_MEASUREMENT.PHYSICAL_METER_ID)).asTable("mm"))
      .on(PHYSICAL_METER.ID.equal(field("mm.physical_meter_id", UUID.class)));
  }
}
