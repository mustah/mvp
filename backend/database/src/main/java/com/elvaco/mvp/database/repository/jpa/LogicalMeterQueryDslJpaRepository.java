package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.jooq.tables.LogicalMeter;
import com.elvaco.mvp.database.entity.jooq.tables.MeterDefinition;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterWithLocation;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.jooq.LogicalMeterJooqConditions;
import com.elvaco.mvp.database.repository.jooq.MeterAlarmJooqConditions;
import com.elvaco.mvp.database.repository.querydsl.LogicalMeterFilterQueryDslVisitor;
import com.elvaco.mvp.database.repository.querydsl.MissingMeasurementFilterQueryDslVisitor;
import com.elvaco.mvp.database.repository.queryfilters.PhysicalMeterStatusLogQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.SelectionQueryFilters;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPADeleteClause;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.elvaco.mvp.core.util.LogicalMeterHelper.withExpectedReadoutsFor;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isLocationQuery;
import static com.querydsl.core.group.GroupBy.groupBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class LogicalMeterQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements LogicalMeterJpaRepository {

  private final DSLContext dsl;

  @Autowired
  LogicalMeterQueryDslJpaRepository(EntityManager entityManager, DSLContext dsl) {
    super(entityManager, LogicalMeterEntity.class);
    this.dsl = dsl;
  }

  @Override
  public Optional<LogicalMeterEntity> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    Predicate predicate = LOGICAL_METER.organisationId.eq(organisationId)
      .and(LOGICAL_METER.id.eq(id));
    return Optional.ofNullable(fetchOne(predicate));
  }

  @Override
  public Optional<LogicalMeterEntity> findBy(UUID organisationId, String externalId) {
    Predicate predicate = LOGICAL_METER.organisationId.eq(organisationId)
      .and(LOGICAL_METER.externalId.eq(externalId));
    return Optional.ofNullable(fetchOne(predicate));
  }

  @Override
  public Optional<LogicalMeterEntity> findBy(RequestParameters parameters) {
    var query = createQuery().select(path);
    new LogicalMeterFilterQueryDslVisitor().visitAndApply(toFilters(parameters), query);
    return Optional.ofNullable(query.distinct().fetchOne());
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
  public Page<LogicalMeterSummaryDto> findAll(RequestParameters parameters, Pageable pageable) {
    var expression = Projections.constructor(
      LogicalMeterSummaryDto.class,
      LOGICAL_METER.id,
      LOGICAL_METER.organisationId,
      LOGICAL_METER.externalId,
      LOGICAL_METER.created,
      Projections.constructor(
        Location.class,
        LOCATION.latitude,
        LOCATION.longitude,
        LOCATION.confidence,
        LOCATION.country,
        LOCATION.city,
        LOCATION.streetAddress
      ),
      LOGICAL_METER.meterDefinition.medium,
      GATEWAY.serial,
      MISSING_MEASUREMENT.count(),
      Projections.constructor(
        AlarmLogEntry.class,
        ALARM_LOG.id,
        ALARM_LOG.physicalMeterId,
        ALARM_LOG.start,
        ALARM_LOG.lastSeen,
        ALARM_LOG.stop,
        ALARM_LOG.mask,
        ALARM_LOG.description
      ).skipNulls(),
      METER_STATUS_LOG.status,
      PHYSICAL_METER.manufacturer,
      PHYSICAL_METER.address,
      PHYSICAL_METER.readIntervalMinutes
    );

    var groupByExpressions = new Expression[] {
      LOGICAL_METER.organisationId,
      LOGICAL_METER.id,
      LOGICAL_METER.externalId,
      LOGICAL_METER.created,
      LOCATION.latitude,
      LOCATION.longitude,
      LOCATION.confidence,
      LOCATION.country,
      LOCATION.city,
      LOCATION.streetAddress,
      LOGICAL_METER.meterDefinition.medium,
      GATEWAY.serial,
      PHYSICAL_METER.manufacturer,
      PHYSICAL_METER.address,
      PHYSICAL_METER.readIntervalMinutes,
      ALARM_LOG.id,
      METER_STATUS_LOG.id
    };

    var countQuery = createCountQuery()
      .select(LOGICAL_METER.id)
      .distinct();

    var query = createQuery()
      .select(expression)
      .groupBy(groupByExpressions)
      .distinct();

    Filters filters = toFilters(parameters);
    new LogicalMeterFilterQueryDslVisitor().visitAndApply(filters, query, countQuery);
    new MissingMeasurementFilterQueryDslVisitor().visitAndApply(filters, query, countQuery);

    querydsl.applyPagination(pageable, query);

    List<LogicalMeterSummaryDto> all = query.fetch();

    var allMeters = parameters.getPeriod()
      .map(period -> all.stream().map(withExpectedReadoutsFor(period)).collect(toList()))
      .orElse(all);

    return getPage(allMeters, pageable, countQuery::fetchCount);
  }

  @Override
  public List<LogicalMeterWithLocation> findAllForSelectionTree(RequestParameters parameters) {
    var logicalMeter = LogicalMeter.LOGICAL_METER;
    var location = com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
    var meterDefinition = MeterDefinition.METER_DEFINITION;

    var query = dsl
      .selectDistinct(
        logicalMeter.ID,
        logicalMeter.ORGANISATION_ID,
        logicalMeter.EXTERNAL_ID,
        location.COUNTRY,
        location.CITY,
        location.STREET_ADDRESS,
        meterDefinition.MEDIUM
      ).from(logicalMeter);

    Filters filters = toFilters(parameters);

    new LogicalMeterJooqConditions().apply(filters, query);
    new MeterAlarmJooqConditions().apply(filters, query);

    return query.fetchInto(LogicalMeterWithLocation.class);
  }

  @Override
  public List<LogicalMeterCollectionStats> findMissingMeterReadingsCounts(
    RequestParameters parameters
  ) {
    var query = createQuery()
      .select(Projections.constructor(
        LogicalMeterCollectionStats.class,
        LOGICAL_METER.id,
        MISSING_MEASUREMENT.id.expectedTime.countDistinct(),
        PHYSICAL_METER.readIntervalMinutes
      ));

    Filters filters = toFilters(parameters);
    new LogicalMeterFilterQueryDslVisitor().visitAndApply(filters, query);
    new MissingMeasurementFilterQueryDslVisitor().visitAndApply(filters, query);

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
    var query = createQuery().select(path);
    new LogicalMeterFilterQueryDslVisitor().visitAndApply(toFilters(parameters), query);
    return query;
  }

  private LogicalMeterEntity fetchOne(Predicate... predicate) {
    return createQuery(predicate).select(path).distinct().fetchOne();
  }

  private static void joinLocation(JPQLQuery<String> query, RequestParameters parameters) {
    if (isLocationQuery(parameters)) {
      query.join(LOGICAL_METER.location, LOCATION);
    }
  }
}
