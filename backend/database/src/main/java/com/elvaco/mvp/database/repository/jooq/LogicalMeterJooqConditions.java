package com.elvaco.mvp.database.repository.jooq;

import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.MeasurementThreshold;
import com.elvaco.mvp.core.filter.AddressFilter;
import com.elvaco.mvp.core.filter.AlarmFilter;
import com.elvaco.mvp.core.filter.CityFilter;
import com.elvaco.mvp.core.filter.FacilityFilter;
import com.elvaco.mvp.core.filter.GatewayIdFilter;
import com.elvaco.mvp.core.filter.LocationConfidenceFilter;
import com.elvaco.mvp.core.filter.LogicalMeterIdFilter;
import com.elvaco.mvp.core.filter.ManufacturerFilter;
import com.elvaco.mvp.core.filter.MeasurementThresholdFilter;
import com.elvaco.mvp.core.filter.MediumFilter;
import com.elvaco.mvp.core.filter.MeterStatusFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.PeriodFilter;
import com.elvaco.mvp.core.filter.SecondaryAddressFilter;
import com.elvaco.mvp.core.filter.SerialFilter;
import com.elvaco.mvp.core.filter.WildcardFilter;
import com.elvaco.mvp.core.util.MeasurementThresholdParser;
import com.elvaco.mvp.database.repository.queryfilters.FilterUtils;
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
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownAddresses;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownCities;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.falseCondition;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.max;

@RequiredArgsConstructor
public class LogicalMeterJooqConditions extends EmptyJooqFilterVisitor {

  public static final Field<Long> MISSING_MEASUREMENT_COUNT = field(
    "missing_measurement_count",
    Long.class
  );

  private final DSLContext dsl;
  private final MeasurementThresholdParser measurementThresholdParser;
  private Condition physicalMeterStatusLogCondition = falseCondition();
  private Condition measurementStatsCondition = falseCondition();
  private Condition missingMeasurementCondition = falseCondition();
  private Condition meterAlarmLogCondition = falseCondition();
  private boolean hasMeasurementStatsFilter = false;

  @Override
  public void visit(CityFilter cityFilter) {
    addCondition(withUnknownCities(toCityParameters(cityFilter.values())));
  }

  @Override
  public void visit(AddressFilter addressFilter) {
    addCondition(withUnknownAddresses(toAddressParameters(addressFilter.values())));
  }

  @Override
  public void visit(OrganisationIdFilter organisationIdFilter) {
    addCondition(LOGICAL_METER.ORGANISATION_ID.in(organisationIdFilter.values()));
  }

  @Override
  public void visit(GatewayIdFilter gatewayIdFilter) {
    addCondition(GATEWAY.ID.equal(gatewayIdFilter.oneValue()));
  }

  @Override
  public void visit(AlarmFilter alarmFilter) {
    addCondition(alarmFilter.values().stream()
      .anyMatch(FilterUtils::isYes)
      ? METER_ALARM_LOG.MASK.isNotNull()
      : METER_ALARM_LOG.MASK.isNull());
  }

  @Override
  public void visit(PeriodFilter periodFilter) {
    var period = periodFilter.getPeriod();

    missingMeasurementCondition =
      MISSING_MEASUREMENT.EXPECTED_TIME.greaterOrEqual(period.start.toOffsetDateTime())
        .and(MISSING_MEASUREMENT.EXPECTED_TIME.lessThan(period.stop.toOffsetDateTime()));

    physicalMeterStatusLogCondition =
      PHYSICAL_METER_STATUS_LOG.START.lessThan(period.stop.toOffsetDateTime())
        .and(PHYSICAL_METER_STATUS_LOG.STOP.isNull()
          .or(PHYSICAL_METER_STATUS_LOG.STOP.greaterOrEqual(period.start.toOffsetDateTime())));

    LocalDate startDate = period.start.toLocalDate();
    LocalDate stopDate = period.stop.toLocalDate();
    if (stopDate.isEqual(startDate)) {
      measurementStatsCondition = MEASUREMENT_STAT_DATA.STAT_DATE.equal(Date.valueOf(startDate));
    } else {
      measurementStatsCondition =
        MEASUREMENT_STAT_DATA.STAT_DATE.greaterOrEqual(Date.valueOf(startDate))
          .and(MEASUREMENT_STAT_DATA.STAT_DATE.lessThan(Date.valueOf(stopDate)))
          .and(MEASUREMENT_STAT_DATA.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID));
    }

    //TODO: This doesn't look right...
    meterAlarmLogCondition = METER_ALARM_LOG.START.between(
      period.start.toOffsetDateTime(),
      period.stop.toOffsetDateTime()
    ).or(METER_ALARM_LOG.STOP.isNull());
  }

  @Override
  public void visit(SerialFilter serialFilter) {
    addCondition(GATEWAY.SERIAL.in(serialFilter.values()));
  }

  @Override
  public void visit(WildcardFilter wildcardFilter) {
    var value = wildcardFilter.oneValue().toLowerCase();
    addCondition(LOGICAL_METER.EXTERNAL_ID.lower().startsWith(value)
      .or(METER_DEFINITION.MEDIUM.lower().startsWith(value))
      .or(LOCATION.CITY.lower().startsWith(value))
      .or(LOCATION.STREET_ADDRESS.lower().startsWith(value))
      .or(PHYSICAL_METER.MANUFACTURER.lower().startsWith(value))
      .or(PHYSICAL_METER.ADDRESS.lower().startsWith(value)));
  }

  @Override
  public void visit(LocationConfidenceFilter locationConfidenceFilter) {
    addCondition(LOCATION.CONFIDENCE.greaterOrEqual(locationConfidenceFilter.oneValue()));
  }

  @Override
  public void visit(MeterStatusFilter meterStatusFilter) {
    addCondition(PHYSICAL_METER_STATUS_LOG.STATUS.in(meterStatusFilter.values()));
  }

  @Override
  public void visit(MediumFilter mediumFilter) {
    addCondition(METER_DEFINITION.MEDIUM.in(mediumFilter.values()));
  }

  @Override
  public void visit(FacilityFilter facilityFilter) {
    addCondition(LOGICAL_METER.EXTERNAL_ID.in(facilityFilter.values()));
  }

  @Override
  public void visit(SecondaryAddressFilter secondaryAddressFilter) {
    addCondition(PHYSICAL_METER.ADDRESS.in(secondaryAddressFilter.values()));
  }

  @Override
  public void visit(ManufacturerFilter manufacturerFilter) {
    addCondition(PHYSICAL_METER.MANUFACTURER.in(manufacturerFilter.values()));
  }

  @Override
  public void visit(LogicalMeterIdFilter logicalMeterIdFilter) {
    addCondition(LOGICAL_METER.ID.in(logicalMeterIdFilter.values()));
  }

  @Override
  public void visit(MeasurementThresholdFilter filter) {
    MeasurementThreshold threshold = measurementThresholdParser.parse(filter.oneValue());

    addCondition(MEASUREMENT_STAT_DATA.QUANTITY.equal(threshold.quantity.getId())
      .and(valueConditionFor(threshold)));

    hasMeasurementStatsFilter = true;
  }

  @Override
  protected <R extends Record> SelectJoinStep<R> applyJoins(SelectJoinStep<R> query) {
    query = query.leftJoin(PHYSICAL_METER)
      .on(PHYSICAL_METER.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)
        .and(PHYSICAL_METER.LOGICAL_METER_ID.equal(LOGICAL_METER.ID))
      )

      .leftJoin(GATEWAYS_METERS)
      .on(GATEWAYS_METERS.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)
        .and(GATEWAYS_METERS.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)))
      .leftJoin(GATEWAY)
      .on(GATEWAY.ORGANISATION_ID.equal(GATEWAYS_METERS.ORGANISATION_ID)
        .and(GATEWAY.ID.equal(GATEWAYS_METERS.GATEWAY_ID)))

      .leftJoin(METER_DEFINITION)
      .on(METER_DEFINITION.TYPE.equal(LOGICAL_METER.METER_DEFINITION_TYPE))

      .leftJoin(LOCATION)
      .on(LOCATION.LOGICAL_METER_ID.equal(LOGICAL_METER.ID)
        .and(LOCATION.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)))

      .leftJoin(PHYSICAL_METER_STATUS_LOG)
      .on(PHYSICAL_METER_STATUS_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
        .and(PHYSICAL_METER_STATUS_LOG.ID.equal(dsl
          .select(max(PHYSICAL_METER_STATUS_LOG.ID))
          .from(PHYSICAL_METER_STATUS_LOG)
          .where(PHYSICAL_METER_STATUS_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
            .and(physicalMeterStatusLogCondition)))))

      .leftJoin(METER_ALARM_LOG)
      .on(METER_ALARM_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
        .and(METER_ALARM_LOG.ID.equal(dsl
          .select(max(METER_ALARM_LOG.ID))
          .from(METER_ALARM_LOG)
          .where(METER_ALARM_LOG.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
            .and(meterAlarmLogCondition)))))

      .leftJoin(lateral(dsl
        .select(count().as(MISSING_MEASUREMENT_COUNT), MISSING_MEASUREMENT.PHYSICAL_METER_ID)
        .from(MISSING_MEASUREMENT)
        .where(MISSING_MEASUREMENT.PHYSICAL_METER_ID.equal(PHYSICAL_METER.ID)
          .and(missingMeasurementCondition))
        .groupBy(MISSING_MEASUREMENT.PHYSICAL_METER_ID)).asTable("mm"))
      .on(PHYSICAL_METER.ID.equal(field("mm.physical_meter_id", UUID.class)));

    if (hasMeasurementStatsFilter && !measurementStatsCondition.equals(falseCondition())) {
      //FIXME: this is messy, maybe put the period into the thresholdFilter instead, and
      // ignore measurement stats when handling the periodFilter
      query.leftJoin(MEASUREMENT_STAT_DATA).on(measurementStatsCondition);
    }
    return query;
  }
}
