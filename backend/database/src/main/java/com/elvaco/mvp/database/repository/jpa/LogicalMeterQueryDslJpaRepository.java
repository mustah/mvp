package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.filter.RequestParametersMapper;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterWithLocation;
import com.elvaco.mvp.database.entity.meter.MeterAlarmLogEntity;
import com.elvaco.mvp.database.entity.meter.PagedLogicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.queryfilters.LogicalMeterQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.MeterAlarmLogQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.MissingMeasurementQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.PhysicalMeterStatusLogQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.SelectionQueryFilters;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPADeleteClause;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static com.elvaco.mvp.core.util.CollectionUtils.isNotEmpty;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isDateRange;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isLocationQuery;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinLogicalMeterGateways;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinLogicalMeterLocation;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinMeterAlarmLogs;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinMetersStatusLogs;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinReportedMeters;
import static com.querydsl.core.group.GroupBy.groupBy;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class LogicalMeterQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements LogicalMeterJpaRepository {

  @Autowired
  LogicalMeterQueryDslJpaRepository(EntityManager entityManager) {
    super(entityManager, LogicalMeterEntity.class);
  }

  @Override
  public Optional<LogicalMeterEntity> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    Predicate predicate = LOGICAL_METER.organisationId.eq(organisationId)
      .and(LOGICAL_METER.id.eq(id));
    return Optional.ofNullable(fetchOne(new RequestParametersAdapter(), predicate));
  }

  @Override
  public Optional<LogicalMeterEntity> findBy(UUID organisationId, String externalId) {
    Predicate predicate = LOGICAL_METER.organisationId.eq(organisationId)
      .and(LOGICAL_METER.externalId.eq(externalId));
    return Optional.ofNullable(fetchOne(new RequestParametersAdapter(), predicate));
  }

  @Override
  public Optional<LogicalMeterEntity> findBy(RequestParameters parameters) {
    return Optional.ofNullable(fetchOne(parameters, meterPredicate(parameters)));
  }

  @Override
  public Page<String> findSecondaryAddresses(RequestParameters parameters, Pageable pageable) {
    return fetchAllBy(parameters, pageable, PHYSICAL_METER.address);
  }

  @Override
  public Page<String> findFacilities(RequestParameters parameters, Pageable pageable) {
    return fetchAllBy(parameters, pageable, PHYSICAL_METER.externalId);
  }

  @Override
  public List<LogicalMeterEntity> findByOrganisationId(UUID organisationId) {
    Predicate predicate = LOGICAL_METER.organisationId.eq(organisationId);
    return createQuery(predicate).select(path).fetch();
  }

  @Override
  public List<LogicalMeterEntity> findAll(RequestParameters parameters) {
    return findAllQuery(parameters).distinct().fetch();
  }

  @Override
  public List<LogicalMeterEntity> findAll(RequestParameters parameters, Sort sort) {
    return querydsl.applySorting(sort, findAllQuery(parameters)).distinct().fetch();
  }

  @Override
  public Page<PagedLogicalMeter> findAll(RequestParameters parameters, Pageable pageable) {
    Filters filters = RequestParametersMapper.toFilters(parameters);

    JPQLQuery<LogicalMeterEntity> countQuery = createCountQuery().select(path).distinct();
    JPQLQuery<PagedLogicalMeter> query = createQuery()
      .select(Projections.constructor(
        PagedLogicalMeter.class,
        LOGICAL_METER.id,
        LOGICAL_METER.organisationId,
        LOGICAL_METER.externalId,
        LOGICAL_METER.created,
        LOGICAL_METER.meterDefinition,
        LOCATION.country,
        LOCATION.city,
        LOCATION.streetAddress,
        PHYSICAL_METER,
        GATEWAY
      ))
      .distinct();

    LogicalMeterFilterQueryDslVisitor visitor = new LogicalMeterFilterQueryDslVisitor();
    visitor.visitAndApply(filters, query, countQuery);

    List<PagedLogicalMeter> all = querydsl.applyPagination(pageable, query).fetch();

    if (isNotEmpty(all)) {
      parameters.setAll(
        LOGICAL_METER_ID,
        all.stream()
          .map(pagedLogicalMeter -> pagedLogicalMeter.id.toString())
          .collect(toList())
      );
      all = fetchAdditionalPagedMeterData(parameters, all);
    }

    return getPage(all, pageable, countQuery::fetchCount);
  }

  @Override
  public List<LogicalMeterWithLocation> findAllForSelectionTree(RequestParameters parameters) {
    JPQLQuery<LogicalMeterWithLocation> query = createQuery(meterPredicate(parameters))
      .select(Projections.constructor(
        LogicalMeterWithLocation.class,
        LOGICAL_METER.id,
        LOGICAL_METER.organisationId,
        LOGICAL_METER.externalId,
        LOCATION.country,
        LOCATION.city,
        LOCATION.streetAddress,
        METER_DEFINITION.medium
      ))
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.statusLogs, METER_STATUS_LOG)
      .on(new PhysicalMeterStatusLogQueryFilters().toPredicate(parameters))
      .leftJoin(LOGICAL_METER.meterDefinition, METER_DEFINITION);

    joinLogicalMeterGateways(query, parameters);
    joinMeterAlarmLogs(query, parameters);

    return query.distinct().fetch();
  }

  @Override
  public List<LogicalMeterCollectionStats> findMissingMeterReadingsCounts(
    RequestParameters parameters
  ) {
    if (!isDateRange(parameters)) {
      return emptyList();
    }

    JPQLQuery<LogicalMeterCollectionStats> query = createQuery(meterPredicate(parameters))
      .select(Projections.constructor(
        LogicalMeterCollectionStats.class,
        LOGICAL_METER.id,
        MISSING_MEASUREMENT.id.expectedTime.countDistinct(),
        PHYSICAL_METER.readIntervalMinutes
      ))
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.missingMeasurements, MISSING_MEASUREMENT)
      .on(new MissingMeasurementQueryFilters().toExpression(parameters));

    joinMeterAlarmLogs(query, parameters);
    joinReportedMeters(query, parameters);
    joinLogicalMeterGateways(query, parameters);
    joinLogicalMeterLocation(query, parameters);

    return query.groupBy(LOGICAL_METER.id, PHYSICAL_METER.readIntervalMinutes)
      .distinct()
      .fetch();
  }

  @Override
  @SuppressWarnings(
    {"SpringDataRepositoryMethodReturnTypeInspection", "SpringDataMethodInconsistencyInspection"}
  )
  public Map<UUID, List<PhysicalMeterStatusLogEntity>> findStatusesGroupedByPhysicalMeterId(
    RequestParameters parameters
  ) {
    return createQuery(new PhysicalMeterStatusLogQueryFilters().toExpression(parameters))
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .join(PHYSICAL_METER.statusLogs, METER_STATUS_LOG)
      .orderBy(METER_STATUS_LOG.start.desc(), METER_STATUS_LOG.stop.desc())
      .transform(groupBy(METER_STATUS_LOG.physicalMeterId).as(GroupBy.list(METER_STATUS_LOG)));
  }

  @Override
  public void delete(UUID id, UUID organisationId) {
    new JPADeleteClause(entityManager, LOGICAL_METER)
      .where(LOGICAL_METER.id.eq(id).and(LOGICAL_METER.organisationId.eq(organisationId)))
      .execute();
  }

  private Page<String> fetchAllBy(
    RequestParameters parameters,
    Pageable pageable,
    StringPath path
  ) {
    var predicate = new SelectionQueryFilters().toExpression(parameters);

    var countQuery = createCountQuery(predicate)
      .select(Projections.constructor(String.class, path))
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .distinct();
    joinLocation(countQuery, parameters);

    var query = createQuery(predicate)
      .select(Projections.constructor(String.class, path))
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .distinct();
    joinLocation(query, parameters);

    var all = querydsl.applyPagination(pageable, query).fetch();

    return getPage(all, pageable, countQuery::fetchCount);
  }

  private JPQLQuery<LogicalMeterEntity> findAllQuery(RequestParameters parameters) {
    Predicate predicate = measurementPredicateOrDefault(parameters);
    JPQLQuery<LogicalMeterEntity> query = createQuery(predicate).select(path)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .fetchJoin();

    joinLogicalMeterGateways(query, parameters);
    joinMeterAlarmLogs(query, parameters);

    return query;
  }

  private Map<UUID, MeterAlarmLogEntity> findAlarms(Predicate predicate) {
    return createQuery(predicate)
      .select(ALARM_LOG.start.max())
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .join(PHYSICAL_METER.alarms, ALARM_LOG)
      .orderBy(ALARM_LOG.start.desc(), ALARM_LOG.stop.desc())
      .transform(groupBy(LOGICAL_METER.id).as(ALARM_LOG));
  }

  private Map<UUID, PhysicalMeterStatusLogEntity> findStatuses(Predicate predicate) {
    return createQuery(predicate)
      .select(METER_STATUS_LOG.start.max())
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .join(PHYSICAL_METER.statusLogs, METER_STATUS_LOG)
      .orderBy(METER_STATUS_LOG.start.desc(), METER_STATUS_LOG.stop.desc())
      .transform(groupBy(LOGICAL_METER.id).as(METER_STATUS_LOG));
  }

  private List<PagedLogicalMeter> fetchAdditionalPagedMeterData(
    RequestParameters parameters,
    List<PagedLogicalMeter> pagedLogicalMeters
  ) {
    Map<UUID, Long> readingCounts =
      findMissingMeterReadingsCounts(parameters).stream()
        .collect(toMap(
          entry -> entry.id,
          entry -> entry.missingReadingCount,
          (oldCount, newCount) -> oldCount + newCount
        ));

    Map<UUID, MeterAlarmLogEntity> alarms =
      findAlarms(new MeterAlarmLogQueryFilters().toExpression(parameters));

    Map<UUID, PhysicalMeterStatusLogEntity> statuses =
      findStatuses(new PhysicalMeterStatusLogQueryFilters().toExpression(parameters));

    return pagedLogicalMeters.stream()
      .map(pagedLogicalMeter -> pagedLogicalMeter
        .withMetaData(
          readingCounts.getOrDefault(pagedLogicalMeter.id, 0L),
          alarms.get(pagedLogicalMeter.id),
          statuses.get(pagedLogicalMeter.id)
        )
      ).collect(toList());
  }

  private Predicate measurementPredicateOrDefault(RequestParameters parameters) {
    return meterPredicate(parameters);
  }

  private LogicalMeterEntity fetchOne(RequestParameters parameters, Predicate... predicate) {
    JPQLQuery<LogicalMeterEntity> query = createQuery(predicate).select(path)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .fetchJoin();

    joinMetersStatusLogs(query, parameters);
    joinLogicalMeterGateways(query, parameters);
    joinMeterAlarmLogs(query, parameters);

    return query.distinct().fetchOne();
  }

  private static void joinLocation(JPQLQuery<String> query, RequestParameters parameters) {
    if (isLocationQuery(parameters)) {
      query.join(LOGICAL_METER.location, LOCATION);
    }
  }

  private static void applyDefaultJoins(JPQLQuery<?> query, RequestParameters parameters) {
    query.leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(LOGICAL_METER.gateways, GATEWAY)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.statusLogs, METER_STATUS_LOG)
      .on(new PhysicalMeterStatusLogQueryFilters().toPredicate(parameters));

    joinMeterAlarmLogs(query, parameters);
    joinLogicalMeterGateways(query, parameters);
  }

  private static Predicate meterPredicate(RequestParameters parameters) {
    return new LogicalMeterQueryFilters().toExpression(parameters);
  }
}
