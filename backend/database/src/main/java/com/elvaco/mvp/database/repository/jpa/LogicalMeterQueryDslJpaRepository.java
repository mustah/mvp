package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.QMissingMeasurementEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterWithLocation;
import com.elvaco.mvp.database.entity.meter.MeterAlarmLogEntity;
import com.elvaco.mvp.database.entity.meter.PagedLogicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity;
import com.elvaco.mvp.database.entity.meter.QMeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.queryfilters.LogicalMeterQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.MeterAlarmLogQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.MissingMeasurementQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.PhysicalMeterStatusLogQueryFilters;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.spi.data.RequestParameter.ID;
import static com.elvaco.mvp.core.spi.data.RequestParameter.MAX_VALUE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.MIN_VALUE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.QUANTITY;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isDateRange;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinGatewayStatusLogs;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinLogicalMeterGateways;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinLogicalMeterLocation;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinLogicalMetersPhysicalMetersStatusLogs;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinMeterStatusLogs;
import static com.querydsl.core.group.GroupBy.groupBy;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class LogicalMeterQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements LogicalMeterJpaRepository {

  private static final QLocationEntity LOCATION =
    QLocationEntity.locationEntity;

  private static final QLogicalMeterEntity LOGICAL_METER =
    QLogicalMeterEntity.logicalMeterEntity;

  private static final QPhysicalMeterStatusLogEntity STATUS_LOG =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  private static final QMeterAlarmLogEntity ALARM_LOG =
    QMeterAlarmLogEntity.meterAlarmLogEntity;

  private static final QPhysicalMeterEntity PHYSICAL_METER =
    QPhysicalMeterEntity.physicalMeterEntity;

  private static final QGatewayEntity GATEWAY =
    QGatewayEntity.gatewayEntity;

  private static final QMeasurementEntity MEASUREMENT =
    QMeasurementEntity.measurementEntity;

  private static final QMissingMeasurementEntity MISSING_MEASUREMENT =
    QMissingMeasurementEntity.missingMeasurementEntity;

  private static final QMeterDefinitionEntity METER_DEFINITION =
    QMeterDefinitionEntity.meterDefinitionEntity;

  @Autowired
  LogicalMeterQueryDslJpaRepository(EntityManager entityManager) {
    super(entityManager, LogicalMeterEntity.class);
  }

  @Override
  public Optional<LogicalMeterEntity> findById(UUID id) {
    return Optional.ofNullable(findOne(id));
  }

  @Override
  public Optional<LogicalMeterEntity> findBy(UUID organisationId, String externalId) {
    Predicate predicate = LOGICAL_METER.organisationId.eq(organisationId)
      .and(LOGICAL_METER.externalId.eq(externalId));
    return Optional.ofNullable(fetchOne(predicate, new RequestParametersAdapter()));
  }

  @Override
  public Optional<LogicalMeterEntity> findBy(RequestParameters parameters) {
    return Optional.ofNullable(fetchOne(
      new LogicalMeterQueryFilters().toExpression(parameters),
      parameters
    ));
  }

  @Override
  public List<LogicalMeterEntity> findByOrganisationId(UUID organisationId) {
    Predicate predicate = LOGICAL_METER.organisationId.eq(organisationId);
    return createQuery(predicate).select(path).fetch();
  }

  @Override
  public List<LogicalMeterEntity> findAll(RequestParameters parameters, Predicate predicate) {
    Predicate measurementPredicate = withMeasurementPredicate(parameters, predicate);
    JPQLQuery<LogicalMeterEntity> query = createQuery(measurementPredicate).select(path);
    return applyJoins(query, parameters).fetch();
  }

  @Override
  public List<LogicalMeterEntity> findAll(
    RequestParameters parameters,
    Predicate predicate,
    Sort sort
  ) {
    Predicate measurementPredicate = withMeasurementPredicate(parameters, predicate);
    JPQLQuery<LogicalMeterEntity> query = createQuery(measurementPredicate).select(path);
    applyJoins(query, parameters);
    return querydsl.applySorting(sort, query).fetch();
  }

  @Override
  public Page<PagedLogicalMeter> findAll(
    RequestParameters parameters,
    Predicate predicate,
    Pageable pageable
  ) {
    predicate = withMeasurementPredicate(parameters, predicate);

    JPQLQuery<LogicalMeterEntity> countQuery = createCountQuery(predicate).select(path)
      .distinct();

    applyDefaultJoins(countQuery, parameters);

    JPQLQuery<PagedLogicalMeter> query = createQuery(predicate)
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

    applyDefaultJoins(query, parameters);

    List<PagedLogicalMeter> all = querydsl.applyPagination(pageable, query).fetch();

    if (!all.isEmpty()) {
      parameters.setAll(
        ID,
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
    Predicate predicate = new LogicalMeterQueryFilters().toExpression(parameters);

    JPQLQuery<LogicalMeterWithLocation> query = createQuery(predicate)
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
      .leftJoin(LOGICAL_METER.meterDefinition, METER_DEFINITION);

    joinMeterStatusLogs(query, parameters);
    joinLogicalMeterGateways(query, parameters);
    joinGatewayStatusLogs(query, parameters);

    return query.distinct().fetch();
  }

  @Override
  public List<LogicalMeterCollectionStats> findMissingMeterReadingsCounts(
    RequestParameters parameters
  ) {
    if (!isDateRange(parameters)) {
      return emptyList();
    }

    Predicate predicate = new LogicalMeterQueryFilters().toExpression(parameters);

    JPQLQuery<LogicalMeterCollectionStats> query = createQuery(predicate)
      .select(Projections.constructor(
        LogicalMeterCollectionStats.class,
        LOGICAL_METER.id,
        MISSING_MEASUREMENT.id.expectedTime.countDistinct(),
        PHYSICAL_METER.readIntervalMinutes
      ))
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.missingMeasurements, MISSING_MEASUREMENT)
      .on(new MissingMeasurementQueryFilters().toExpression(parameters))
      .groupBy(LOGICAL_METER.id, PHYSICAL_METER.readIntervalMinutes);

    joinLogicalMeterGateways(query, parameters);
    joinLogicalMeterLocation(query, parameters);
    joinGatewayStatusLogs(query, parameters);
    joinMeterStatusLogs(query, parameters);

    return query.fetch();
  }

  @Override
  @SuppressWarnings(
    {"SpringDataRepositoryMethodReturnTypeInspection", "SpringDataMethodInconsistencyInspection"}
  )
  public Map<UUID, List<PhysicalMeterStatusLogEntity>> findStatusesGroupedByPhysicalMeterId(
    Predicate predicate
  ) {
    return createQuery(predicate)
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .join(PHYSICAL_METER.statusLogs, STATUS_LOG)
      .orderBy(STATUS_LOG.start.desc(), STATUS_LOG.stop.desc())
      .transform(groupBy(STATUS_LOG.physicalMeterId).as(GroupBy.list(STATUS_LOG)));
  }

  @Override
  public void delete(UUID id, UUID organisationId) {
    new JPADeleteClause(entityManager, LOGICAL_METER)
      .where(LOGICAL_METER.id.eq(id).and(LOGICAL_METER.organisationId.eq(organisationId)))
      .execute();
  }

  private Map<UUID, PhysicalMeterStatusLogEntity> findCurrentStatuses(Predicate predicate) {
    return createQuery(predicate)
      .select(STATUS_LOG.start.max())
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .join(PHYSICAL_METER.statusLogs, STATUS_LOG)
      .orderBy(STATUS_LOG.start.desc(), STATUS_LOG.stop.desc())
      .transform(groupBy(LOGICAL_METER.id).as(STATUS_LOG));
  }

  private Map<UUID, MeterAlarmLogEntity> findAlarms(Predicate predicate) {
    return createQuery(predicate)
      .select(ALARM_LOG.start.max())
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .join(PHYSICAL_METER.alarms, ALARM_LOG)
      .orderBy(ALARM_LOG.start.desc(), ALARM_LOG.stop.desc())
      .transform(groupBy(LOGICAL_METER.id).as(ALARM_LOG));
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

    Map<UUID, PhysicalMeterStatusLogEntity> statuses =
      findCurrentStatuses(new PhysicalMeterStatusLogQueryFilters().toExpression(parameters));

    Map<UUID, MeterAlarmLogEntity> alarms =
      findAlarms(new MeterAlarmLogQueryFilters().toExpression(parameters));

    return pagedLogicalMeters.stream()
      .map(pagedLogicalMeter -> pagedLogicalMeter
        .withMetaData(
          readingCounts.getOrDefault(pagedLogicalMeter.id, 0L),
          statuses.get(pagedLogicalMeter.id),
          alarms.get(pagedLogicalMeter.id)
        )
      ).collect(toList());
  }

  private Predicate withMeasurementPredicate(
    RequestParameters parameters,
    Predicate predicate
  ) {
    if ((parameters.hasParam(MIN_VALUE) || parameters.hasParam(MAX_VALUE))
      && parameters.hasParam(QUANTITY)) {

      String quantity = parameters.getFirst(QUANTITY);

      JPAQuery<UUID> queryIds = new JPAQueryFactory(entityManager)
        .select(LOGICAL_METER.id).from(path)
        .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
        .join(PHYSICAL_METER.measurements, MEASUREMENT)
        .where(withMeasurementAboveMax(
          parameters,
          withMeasurementBelowMin(parameters, MEASUREMENT.id.quantity.name.eq(quantity))
        ));

      return LOGICAL_METER.id.in(queryIds);
    } else {
      return predicate;
    }
  }

  private Predicate withMeasurementBelowMin(RequestParameters parameters, Predicate predicate) {
    return Optional.ofNullable(parameters.getFirst(MIN_VALUE))
      .map(minVal -> (Predicate) Expressions.booleanOperation(
        Ops.LT,
        MEASUREMENT.value,
        Expressions.simpleTemplate(MeasurementUnit.class, "{0}", MeasurementUnit.from(minVal))
        ).and(predicate)
      ).orElse(predicate);
  }

  private Predicate withMeasurementAboveMax(RequestParameters parameters, Predicate predicate) {
    return Optional.ofNullable(parameters.getFirst(MAX_VALUE))
      .map(maxVal -> (Predicate) Expressions.booleanOperation(
        Ops.GT,
        MEASUREMENT.value,
        Expressions.simpleTemplate(MeasurementUnit.class, "{0}", MeasurementUnit.from(maxVal))
        ).and(predicate)
      ).orElse(predicate);
  }

  private LogicalMeterEntity fetchOne(Predicate predicate, RequestParameters parameters) {
    JPQLQuery<LogicalMeterEntity> query = createQuery(predicate).select(path);
    return applyJoins(query, parameters).fetchOne();
  }

  private static void applyDefaultJoins(JPQLQuery<?> query, RequestParameters parameters) {
    query
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(LOGICAL_METER.gateways, GATEWAY)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG)
      .leftJoin(PHYSICAL_METER.alarms, ALARM_LOG);

    joinLogicalMeterGateways(query, parameters);
  }

  private static <T> JPQLQuery<T> applyJoins(JPQLQuery<T> query, RequestParameters parameters) {
    query = query
      .distinct()
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .fetchJoin();

    joinLogicalMetersPhysicalMetersStatusLogs(query, parameters);
    joinLogicalMeterGateways(query, parameters);

    return query;
  }
}
