package com.elvaco.mvp.database.repository.querydsl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
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
import com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.queryfilters.LocationPredicates;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.alarmQueryFilter;

public class LogicalMeterFilterQueryDslVisitor extends FilterQueryDslJpaVisitor {

  private static final QPhysicalMeterStatusLogEntity METER_STATUS_LOG_JOIN =
    new QPhysicalMeterStatusLogEntity("meter_status_log_join");

  private static final QMeterAlarmLogEntity METER_ALARM_LOG_JOIN =
    new QMeterAlarmLogEntity("meter_alarm_log_join");

  private final Collection<Predicate> predicates = new ArrayList<>();

  private Predicate alarmLogPredicate = FALSE_PREDICATE;
  private Predicate statusLogPredicate = FALSE_PREDICATE;

  @Override
  public void visit(CityFilter cityFilter) {
    Optional.ofNullable(LocationPredicates.whereCityOrUnknown(cityFilter.values()))
      .ifPresent(predicates::add);
  }

  @Override
  public void visit(AddressFilter addressFilter) {
    Optional.ofNullable(LocationPredicates.whereAddressOrUnknown(addressFilter.values()))
      .ifPresent(predicates::add);
  }

  @Override
  public void visit(OrganisationIdFilter organisationIdFilter) {
    predicates.add(LOGICAL_METER.organisationId.in(organisationIdFilter.values()));
  }

  @Override
  public void visit(GatewayIdFilter gatewayIdFilter) {
    predicates.add(GATEWAY.id.eq(gatewayIdFilter.oneValue()));
  }

  @Override
  public void visit(AlarmFilter alarmFilter) {
    predicates.add(alarmQueryFilter(alarmFilter.values()));
  }

  @Override
  public void visit(PeriodFilter periodFilter) {
    SelectionPeriod period = periodFilter.getPeriod();
    alarmLogPredicate = withinPeriod(period, ALARM_LOG.start, ALARM_LOG.stop);
    statusLogPredicate = withinPeriod(period, METER_STATUS_LOG.start, METER_STATUS_LOG.stop);
  }

  @Override
  public void visit(SerialFilter serialFilter) {
    predicates.add(GATEWAY.serial.in(serialFilter.values()));
  }

  @Override
  public void visit(WildcardFilter wildcardFilter) {
    String str = wildcardFilter.oneValue();
    predicates.add(LOGICAL_METER.externalId.startsWithIgnoreCase(str)
      .or(LOGICAL_METER.meterDefinition.medium.startsWithIgnoreCase(str))
      .or(LOGICAL_METER.location.city.startsWithIgnoreCase(str))
      .or(LOGICAL_METER.location.streetAddress.startsWithIgnoreCase(str))
      .or(PHYSICAL_METER.manufacturer.startsWithIgnoreCase(str))
      .or(PHYSICAL_METER.address.startsWithIgnoreCase(str)));
  }

  @Override
  public void visit(LocationConfidenceFilter locationConfidenceFilter) {
    predicates.add(LOCATION.confidence.goe(locationConfidenceFilter.oneValue()));
  }

  @Override
  public void visit(MeterStatusFilter meterStatusFilter) {
    predicates.add(METER_STATUS_LOG.status.in(meterStatusFilter.values()));
  }

  @Override
  public void visit(MediumFilter mediumFilter) {
    predicates.add(LOGICAL_METER.meterDefinition.medium.in(mediumFilter.values()));
  }

  @Override
  public void visit(FacilityFilter facilityFilter) {
    predicates.add(LOGICAL_METER.externalId.in(facilityFilter.values()));
  }

  @Override
  public void visit(SecondaryAddressFilter secondaryAddressFilter) {
    predicates.add(PHYSICAL_METER.address.in(secondaryAddressFilter.values()));
  }

  @Override
  public void visit(ManufacturerFilter manufacturerFilter) {
    predicates.add(PHYSICAL_METER.manufacturer.in(manufacturerFilter.values()));
  }

  @Override
  public void visit(LogicalMeterIdFilter logicalMeterIdFilter) {
    predicates.add(LOGICAL_METER.id.in(logicalMeterIdFilter.values()));
  }

  @Override
  protected void applyJoins(JPQLQuery<?> q) {
    q.leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(LOGICAL_METER.gateways, GATEWAY)
      .leftJoin(LOGICAL_METER.meterDefinition, METER_DEFINITION)
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(PHYSICAL_METER.statusLogs, METER_STATUS_LOG)
      .on(statusLogPredicate)
      .leftJoin(PHYSICAL_METER.statusLogs, METER_STATUS_LOG_JOIN)
      .on(METER_STATUS_LOG_JOIN.start.gt(METER_STATUS_LOG.start)
        .or(METER_STATUS_LOG_JOIN.start.eq(METER_STATUS_LOG.start))
        .and(METER_STATUS_LOG_JOIN.id.gt(METER_STATUS_LOG.id)))
      .leftJoin(PHYSICAL_METER.alarms, ALARM_LOG)
      .on(alarmLogPredicate)
      .leftJoin(PHYSICAL_METER.alarms, METER_ALARM_LOG_JOIN)
      .on(METER_ALARM_LOG_JOIN.start.gt(ALARM_LOG.start)
        .or(METER_ALARM_LOG_JOIN.start.eq(ALARM_LOG.start))
        .and(METER_ALARM_LOG_JOIN.id.gt(ALARM_LOG.id)))
      .where(METER_ALARM_LOG_JOIN.id.isNull().and(METER_STATUS_LOG_JOIN.id.isNull()));
  }

  @Override
  protected Collection<Predicate> getPredicates() {
    return new ArrayList<>(predicates);
  }
}
