package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.filter.AddressFilter;
import com.elvaco.mvp.core.filter.AlarmFilter;
import com.elvaco.mvp.core.filter.CityFilter;
import com.elvaco.mvp.core.filter.FacilityFilter;
import com.elvaco.mvp.core.filter.GatewayIdFilter;
import com.elvaco.mvp.core.filter.LocationConfidenceFilter;
import com.elvaco.mvp.core.filter.LogicalMeterIdFilter;
import com.elvaco.mvp.core.filter.ManufacturerFilter;
import com.elvaco.mvp.core.filter.MediumFilter;
import com.elvaco.mvp.core.filter.MeterStatusFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.PeriodFilter;
import com.elvaco.mvp.core.filter.SecondaryAddressFilter;
import com.elvaco.mvp.core.filter.SerialFilter;
import com.elvaco.mvp.core.filter.WildcardFilter;
import com.elvaco.mvp.database.repository.queryfilters.FilterUtils;
import org.jooq.Record;
import org.jooq.SelectJoinStep;

import static com.elvaco.mvp.database.entity.jooq.Tables.GATEWAY;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_ALARM_LOG;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.GatewaysMeters.GATEWAYS_METERS;
import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeterStatusLog.PHYSICAL_METER_STATUS_LOG;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownAddresses;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownCities;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;

public class LogicalMeterJooqConditions extends EmptyJooqFilterVisitor {

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

    var withinDateRange = PHYSICAL_METER_STATUS_LOG.START.between(
      period.start.toOffsetDateTime(),
      period.stop.toOffsetDateTime()
    ).or(PHYSICAL_METER_STATUS_LOG.STOP.isNull());

    addCondition(withinDateRange);
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
      .or(LOCATION.COUNTRY.lower().startsWith(value))
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
  protected <R extends Record> SelectJoinStep<R> applyJoins(SelectJoinStep<R> query) {
    return query.leftJoin(PHYSICAL_METER)
      .on(PHYSICAL_METER.LOGICAL_METER_ID.eq(LOGICAL_METER.ID)
        .and(PHYSICAL_METER.ORGANISATION_ID.eq(LOGICAL_METER.ORGANISATION_ID))
      )
      .leftJoin(GATEWAYS_METERS)
      .on(GATEWAYS_METERS.LOGICAL_METER_ID.eq(LOGICAL_METER.ID))
      .leftJoin(GATEWAY)
      .on(GATEWAY.ID.equal(GATEWAYS_METERS.GATEWAY_ID))
      .leftJoin(METER_DEFINITION)
      .on(METER_DEFINITION.TYPE.eq(LOGICAL_METER.METER_DEFINITION_TYPE))
      .leftJoin(PHYSICAL_METER_STATUS_LOG)
      .on(PHYSICAL_METER_STATUS_LOG.PHYSICAL_METER_ID.eq(PHYSICAL_METER.ID))
      .leftJoin(LOCATION)
      .on(LOCATION.LOGICAL_METER_ID.eq(LOGICAL_METER.ID));
  }
}
