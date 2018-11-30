package com.elvaco.mvp.database.repository.jooq;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.filter.AddressFilter;
import com.elvaco.mvp.core.filter.AlarmFilter;
import com.elvaco.mvp.core.filter.CityFilter;
import com.elvaco.mvp.core.filter.FacilityFilter;
import com.elvaco.mvp.core.filter.GatewayIdFilter;
import com.elvaco.mvp.core.filter.LocationConfidenceFilter;
import com.elvaco.mvp.core.filter.ManufacturerFilter;
import com.elvaco.mvp.core.filter.MediumFilter;
import com.elvaco.mvp.core.filter.MeterStatusFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.PeriodFilter;
import com.elvaco.mvp.core.filter.SecondaryAddressFilter;
import com.elvaco.mvp.core.filter.SerialFilter;
import com.elvaco.mvp.core.filter.WildcardFilter;
import com.elvaco.mvp.database.entity.jooq.tables.GatewaysMeters;
import com.elvaco.mvp.database.repository.queryfilters.FilterUtils;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;

import static com.elvaco.mvp.database.entity.jooq.Tables.GATEWAY;
import static com.elvaco.mvp.database.entity.jooq.Tables.GATEWAYS_METERS;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.Tables.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_ALARM_LOG;
import static com.elvaco.mvp.database.entity.jooq.Tables.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER_STATUS_LOG;
import static com.elvaco.mvp.database.entity.jooq.tables.GatewayStatusLog.GATEWAY_STATUS_LOG;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownAddresses;
import static com.elvaco.mvp.database.repository.queryfilters.LocationConditions.withUnknownCities;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;
import static org.jooq.impl.DSL.falseCondition;

@RequiredArgsConstructor
public class GatewayJooqConditions extends EmptyJooqFilterVisitor {
  private final DSLContext dsl;

  private Condition gatewayStatusLogCondition = falseCondition();
  private Condition alarmLogCondition = falseCondition();
  private Condition meterStatusLogCondition = falseCondition();

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
    addCondition(GATEWAY.ORGANISATION_ID.in(organisationIdFilter.values()));
  }

  @Override
  public void visit(GatewayIdFilter gatewayIdFilter) {
    addCondition(GATEWAY.ID.in(gatewayIdFilter.values()));
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
    SelectionPeriod period = periodFilter.getPeriod();

    meterStatusLogCondition =
      PHYSICAL_METER_STATUS_LOG.START.lessThan(period.stop.toOffsetDateTime())
        .and(PHYSICAL_METER_STATUS_LOG.STOP.isNull()
          .or(PHYSICAL_METER_STATUS_LOG.STOP.greaterOrEqual(period.start.toOffsetDateTime())));

    gatewayStatusLogCondition =
      GATEWAY_STATUS_LOG.START.lessThan(period.stop.toOffsetDateTime())
        .and(GATEWAY_STATUS_LOG.STOP.isNull()
          .or(GATEWAY_STATUS_LOG.STOP.greaterOrEqual(period.start.toOffsetDateTime())));

    alarmLogCondition = METER_ALARM_LOG.START.between(
      period.start.toOffsetDateTime(),
      period.stop.toOffsetDateTime()
    ).or(METER_ALARM_LOG.STOP.isNull());
  }

  @Override
  public void visit(SerialFilter serialFilter) {
    if (serialFilter.isWildcard()) {
      addCondition(GATEWAY.SERIAL.containsIgnoreCase(serialFilter.oneValue()));
    } else {
      addCondition(GATEWAY.SERIAL.in(serialFilter.values()));
    }
  }

  @Override
  public void visit(WildcardFilter wildcardFilter) {
    var value = wildcardFilter.oneValue().toLowerCase();

    addCondition(GATEWAY.SERIAL.lower().startsWith(value)
      .or(GATEWAY.PRODUCT_MODEL.lower().startsWith(value))
      .or(LOCATION.CITY.lower().startsWith(value))
      .or(LOCATION.STREET_ADDRESS.lower().startsWith(value)));
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
  protected <R extends Record> SelectJoinStep<R> applyJoins(SelectJoinStep<R> query) {
    return query.leftJoin(GATEWAY_STATUS_LOG)
      .on(GATEWAY_STATUS_LOG.GATEWAY_ID.eq(GATEWAY.ID)
        .and(GATEWAY_STATUS_LOG.ID.eq(dsl
          .select(DSL.max(GATEWAY_STATUS_LOG.ID))
          .from(GATEWAY_STATUS_LOG)
          .where(GATEWAY_STATUS_LOG.GATEWAY_ID.eq(GATEWAY.ID)
            .and(gatewayStatusLogCondition)))))

      .leftJoin(GATEWAYS_METERS)
      .on(GATEWAYS_METERS.GATEWAY_ID.equal(GATEWAY.ID)
        .and(GATEWAYS_METERS.ORGANISATION_ID.eq(GATEWAY.ORGANISATION_ID)))

      .leftJoin(LOGICAL_METER)
      .on(LOGICAL_METER.ID.equal(GatewaysMeters.GATEWAYS_METERS.LOGICAL_METER_ID)
        .and(GATEWAY.ORGANISATION_ID.equal(LOGICAL_METER.ORGANISATION_ID)))

      .leftJoin(PHYSICAL_METER).on(PHYSICAL_METER.LOGICAL_METER_ID.eq(LOGICAL_METER.ID)
        .and(PHYSICAL_METER.ORGANISATION_ID.eq(LOGICAL_METER.ORGANISATION_ID)))

      .leftJoin(METER_DEFINITION)
      .on(METER_DEFINITION.TYPE.eq(LOGICAL_METER.METER_DEFINITION_TYPE))

      .leftJoin(PHYSICAL_METER_STATUS_LOG)
      .on(PHYSICAL_METER_STATUS_LOG.PHYSICAL_METER_ID.eq(PHYSICAL_METER.ID)
        .and(PHYSICAL_METER_STATUS_LOG.ID.eq(dsl
          .select(DSL.max(PHYSICAL_METER_STATUS_LOG.ID))
          .from(PHYSICAL_METER_STATUS_LOG)
          .where(PHYSICAL_METER_STATUS_LOG.PHYSICAL_METER_ID.eq(PHYSICAL_METER.ID)
            .and(meterStatusLogCondition)))))

      .leftJoin(LOCATION).on(LOCATION.LOGICAL_METER_ID.eq(LOGICAL_METER.ID))
      .leftJoin(METER_ALARM_LOG).on(alarmLogCondition);
  }
}
