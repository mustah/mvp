package com.elvaco.mvp.database.repository.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.filter.AddressFilter;
import com.elvaco.mvp.core.filter.AlarmFilter;
import com.elvaco.mvp.core.filter.CityFilter;
import com.elvaco.mvp.core.filter.GatewayIdFilter;
import com.elvaco.mvp.core.filter.LocationConfidenceFilter;
import com.elvaco.mvp.core.filter.MeterStatusFilter;
import com.elvaco.mvp.core.filter.OrganisationIdFilter;
import com.elvaco.mvp.core.filter.PeriodFilter;
import com.elvaco.mvp.core.filter.SerialFilter;
import com.elvaco.mvp.core.filter.WildcardFilter;
import com.elvaco.mvp.database.repository.queryfilters.LocationPredicates;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;

import static com.elvaco.mvp.core.filter.ComparisonMode.EQUAL;
import static com.elvaco.mvp.core.filter.ComparisonMode.WILDCARD;
import static com.elvaco.mvp.database.repository.jpa.BaseQueryDslRepository.ALARM_LOG;
import static com.elvaco.mvp.database.repository.jpa.BaseQueryDslRepository.GATEWAY;
import static com.elvaco.mvp.database.repository.jpa.BaseQueryDslRepository.GATEWAY_STATUS_LOG;
import static com.elvaco.mvp.database.repository.jpa.BaseQueryDslRepository.LOCATION;
import static com.elvaco.mvp.database.repository.jpa.BaseQueryDslRepository.LOGICAL_METER;
import static com.elvaco.mvp.database.repository.jpa.BaseQueryDslRepository.METER_STATUS_LOG;
import static com.elvaco.mvp.database.repository.jpa.BaseQueryDslRepository.PHYSICAL_METER;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.alarmQueryFilter;

class GatewayFilterQueryDslJpaVisitor extends FilterQueryDslJpaVisitor {

  private Predicate alarmLogPredicate = FALSE_PREDICATE;
  private Predicate statusLogPredicate = FALSE_PREDICATE;
  private Predicate meterStatusLogPredicate = FALSE_PREDICATE;
  private List<Predicate> predicates = new ArrayList<>();

  @Override
  public void visit(CityFilter cityFilter) {
    Optional.ofNullable(
      LocationPredicates.whereCityOrUnknown(cityFilter.values())
    ).ifPresent(predicates::add);
  }

  @Override
  public void visit(AddressFilter addressFilter) {
    Optional.ofNullable(
      LocationPredicates.whereAddressOrUnknown(addressFilter.values())
    ).ifPresent(predicates::add);
  }

  @Override
  public void visit(OrganisationIdFilter organisationIdFilter) {
    predicates.add(GATEWAY.organisationId.in(organisationIdFilter.values()));
  }

  @Override
  public void visit(GatewayIdFilter gatewayIdFilter) {
    predicates.add(GATEWAY.id.in(gatewayIdFilter.values()));
  }

  @Override
  public void visit(AlarmFilter alarmFilter) {
    predicates.add(alarmQueryFilter(alarmFilter.values()));
  }

  @Override
  public void visit(PeriodFilter periodFilter) {
    SelectionPeriod period = periodFilter.getPeriod();
    alarmLogPredicate = inPeriod(period, ALARM_LOG.start, ALARM_LOG.stop);
    statusLogPredicate = inPeriod(period, GATEWAY_STATUS_LOG.start, GATEWAY_STATUS_LOG.stop);
    meterStatusLogPredicate = inPeriod(period, METER_STATUS_LOG.start, METER_STATUS_LOG.stop);
  }

  @Override
  public void visit(SerialFilter serialFilter) {
    if (serialFilter.mode() == EQUAL) {
      predicates.add(GATEWAY.serial.in(serialFilter.values()));
    } else if (serialFilter.mode() == WILDCARD) {
      predicates.add(GATEWAY.serial.containsIgnoreCase(serialFilter.oneValue()));
    }
  }

  @Override
  public void visit(WildcardFilter wildcardFilter) {
    String str = wildcardFilter.oneValue();

    predicates.add(GATEWAY.serial.startsWithIgnoreCase(str)
      .or(GATEWAY.productModel.startsWithIgnoreCase(str))
      .or(LOCATION.city.startsWithIgnoreCase(str))
      .or(LOCATION.streetAddress.startsWithIgnoreCase(str)));
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
  List<Predicate> getPredicates() {
    return predicates;
  }

  @Override
  void applyJoins(JPQLQuery<?> q) {
    q.leftJoin(GATEWAY.statusLogs, GATEWAY_STATUS_LOG).on(statusLogPredicate)
      .leftJoin(GATEWAY.meters, LOGICAL_METER)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(PHYSICAL_METER.alarms, ALARM_LOG).on(alarmLogPredicate)
      .leftJoin(PHYSICAL_METER.statusLogs, METER_STATUS_LOG).on(meterStatusLogPredicate);
  }
}
