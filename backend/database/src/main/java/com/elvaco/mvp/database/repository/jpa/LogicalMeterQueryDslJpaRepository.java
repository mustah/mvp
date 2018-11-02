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
import com.elvaco.mvp.database.entity.meter.PagedLogicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.queryfilters.LogicalMeterQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.MissingMeasurementQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.PhysicalMeterStatusLogQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.SelectionQueryFilters;
import com.querydsl.core.ResultTransformer;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.ConstructorExpression;
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

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isDateRange;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isLocationQuery;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinLogicalMeterGateways;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinLogicalMeterLocation;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinMeterAlarmLogs;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinMetersStatusLogs;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinReportedMeters;
import static com.querydsl.core.group.GroupBy.groupBy;
import static java.util.Collections.emptyList;
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

    ConstructorExpression<PagedLogicalMeter> constructor = Projections.constructor(
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
      GATEWAY,
      GroupBy.set(MISSING_MEASUREMENT),
      GroupBy.set(ALARM_LOG),
      GroupBy.set(METER_STATUS_LOG)
    );

    JPQLQuery<PagedLogicalMeter> countQuery = createCountQuery().select(constructor).distinct();
    JPQLQuery<PagedLogicalMeter> query = createQuery()
      .select(constructor)
      .distinct();

    LogicalMeterFilterQueryDslVisitor visitor = new LogicalMeterFilterQueryDslVisitor();
    visitor.visitAndApply(filters, query, countQuery);
    querydsl.applyPagination(pageable, query);

    ResultTransformer<List<PagedLogicalMeter>> transformer = GroupBy.groupBy(
      LOGICAL_METER.id,
      PHYSICAL_METER.id
    ).list(constructor);
    List<PagedLogicalMeter> all = query.transform(transformer);

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
    Predicate predicate = meterPredicate(parameters);
    JPQLQuery<LogicalMeterEntity> query = createQuery(predicate).select(path)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .fetchJoin();

    joinLogicalMeterGateways(query, parameters);
    joinMeterAlarmLogs(query, parameters);

    return query;
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

  private static Predicate meterPredicate(RequestParameters parameters) {
    return new LogicalMeterQueryFilters().toExpression(parameters);
  }
}
