package com.elvaco.mvp.database.repository.jpa;

import java.util.ArrayList;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
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
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;

import static com.elvaco.mvp.database.repository.jpa.BaseQueryDslRepository.ALARM_LOG;
import static com.elvaco.mvp.database.repository.jpa.BaseQueryDslRepository.GATEWAY;
import static com.elvaco.mvp.database.repository.jpa.BaseQueryDslRepository.LOCATION;
import static com.elvaco.mvp.database.repository.jpa.BaseQueryDslRepository.LOGICAL_METER;
import static com.elvaco.mvp.database.repository.jpa.BaseQueryDslRepository.METER_STATUS_LOG;
import static com.elvaco.mvp.database.repository.jpa.BaseQueryDslRepository.PHYSICAL_METER;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.alarmQueryFilter;

class LogicalMeterFilterQueryDslVisitor extends FilterQueryDslJpaVisitor {
  private Predicate alarmLogPredicate = FALSE_PREDICATE;
  private Predicate statusLogPredicate = FALSE_PREDICATE;
  private List<Predicate> predicates = new ArrayList<>();

  @Override
  public void visit(CityFilter cityFilter) {
    //predicates.add(LocationPredicates.whereCityOrUnknown(cityFilter.values()));
  }

  @Override
  public void visit(AddressFilter addressFilter) {

  }

  @Override
  public void visit(OrganisationIdFilter organisationIdFilter) {
    predicates.add(LOGICAL_METER.organisationId.in(organisationIdFilter.values()));
  }

  @Override
  public void visit(GatewayIdFilter gatewayIdFilter) {

  }

  @Override
  public void visit(AlarmFilter alarmFilter) {
    predicates.add(alarmQueryFilter(alarmFilter.values()));
  }

  @Override
  public void visit(PeriodFilter periodFilter) {
    SelectionPeriod period = periodFilter.getPeriod();
    alarmLogPredicate = inPeriod(period, ALARM_LOG.start, ALARM_LOG.stop);
    statusLogPredicate = inPeriod(period, METER_STATUS_LOG.start, METER_STATUS_LOG.stop);
  }

  @Override
  public void visit(SerialFilter serialFilter) {

  }

  @Override
  public void visit(WildcardFilter wildcardFilter) {

  }

  @Override
  public void visit(LocationConfidenceFilter locationConfidenceFilter) {

  }

  @Override
  public void visit(MeterStatusFilter meterStatusFilter) {

  }

  @Override
  List<Predicate> getPredicates() {
    return predicates;
  }

  @Override
  void applyJoins(JPQLQuery<?> q) {
    q.join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(LOGICAL_METER.gateways, GATEWAY)
      .leftJoin(PHYSICAL_METER.statusLogs, METER_STATUS_LOG)
      .on(statusLogPredicate)
      .join(LOGICAL_METER.location, LOCATION)
      .on(LOCATION.confidence.goe(GeoCoordinate.HIGH_CONFIDENCE))
      .leftJoin(PHYSICAL_METER.alarms, ALARM_LOG)
      .on(alarmLogPredicate);
  }
}
