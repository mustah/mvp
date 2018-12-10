package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.jooq.tables.Gateway;
import com.elvaco.mvp.database.entity.jooq.tables.LogicalMeter;
import com.elvaco.mvp.database.entity.jooq.tables.MeterAlarmLog;
import com.elvaco.mvp.database.entity.jooq.tables.MeterDefinition;
import com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeter;
import com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeterStatusLog;
import com.elvaco.mvp.database.entity.jooq.tables.records.PhysicalMeterRecord;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterWithLocation;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.jooq.JooqFilterVisitor;
import com.elvaco.mvp.database.repository.jooq.SelectionJooqConditions;
import com.elvaco.mvp.database.repository.queryfilters.PhysicalMeterStatusLogQueryFilters;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPADeleteClause;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.TableField;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.elvaco.mvp.core.util.ExpectedReadouts.expectedReadouts;
import static com.elvaco.mvp.database.repository.jooq.LogicalMeterJooqConditions.MISSING_MEASUREMENT_COUNT;
import static com.querydsl.core.group.GroupBy.groupBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class LogicalMeterQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements LogicalMeterJpaRepository {

  private final DSLContext dsl;
  private final JooqFilterVisitor logicalMeterJooqConditions;

  @Autowired
  LogicalMeterQueryDslJpaRepository(
    EntityManager entityManager,
    DSLContext dsl,
    JooqFilterVisitor logicalMeterJooqConditions
  ) {
    super(entityManager, LogicalMeterEntity.class);
    this.dsl = dsl;
    this.logicalMeterJooqConditions = logicalMeterJooqConditions;
  }

  @Override
  public Optional<LogicalMeterEntity> findById(UUID id) {
    var logicalMeter = LogicalMeter.LOGICAL_METER;
    return fetchOne(logicalMeter.ID.equal(id));
  }

  @Override
  public Optional<LogicalMeterEntity> findByPrimaryKey(UUID organisationId, UUID id) {
    var logicalMeter = LogicalMeter.LOGICAL_METER;
    Condition condition = logicalMeter.ORGANISATION_ID.equal(organisationId)
      .and(logicalMeter.ID.equal(id));
    return fetchOne(condition);
  }

  @Override
  public Optional<LogicalMeterEntity> findBy(UUID organisationId, String externalId) {
    var logicalMeter = LogicalMeter.LOGICAL_METER;
    Condition condition = logicalMeter.ORGANISATION_ID.equal(organisationId)
      .and(logicalMeter.EXTERNAL_ID.equal(externalId));
    return fetchOne(condition);
  }

  @Override
  public Optional<LogicalMeterEntity> findBy(RequestParameters parameters) {
    var logicalMeter = LogicalMeter.LOGICAL_METER;

    SelectJoinStep<Record> sqlGenerator = dsl.select()
      .from(logicalMeter);

    logicalMeterJooqConditions.apply(toFilters(parameters), sqlGenerator);

    final String sql = sqlGenerator.getSQL(ParamType.NAMED);
    Query query = entityManager.createNativeQuery(sql, LogicalMeterEntity.class);

    sqlGenerator.getParams().forEach(query::setParameter);

    return Optional.ofNullable((LogicalMeterEntity) query.getSingleResult());
  }

  @Override
  public Page<String> findSecondaryAddresses(RequestParameters parameters, Pageable pageable) {
    return fetchAllBy(parameters, pageable, PhysicalMeter.PHYSICAL_METER.ADDRESS);
  }

  @Override
  public Page<String> findFacilities(RequestParameters parameters, Pageable pageable) {
    return fetchAllBy(parameters, pageable, PhysicalMeter.PHYSICAL_METER.EXTERNAL_ID);
  }

  @Override
  public List<LogicalMeterEntity> findByOrganisationId(UUID organisationId) {
    Predicate predicate = LOGICAL_METER.pk.organisationId.eq(organisationId);
    return createQuery(predicate).select(path).fetch();
  }

  @Override
  public List<LogicalMeterEntity> findAll(RequestParameters parameters) {
    var logicalMeter = LogicalMeter.LOGICAL_METER;

    var query = dsl.select()
      .from(logicalMeter);

    logicalMeterJooqConditions.apply(toFilters(parameters), query);

    return nativeQuery(query, LogicalMeterEntity.class);
  }

  @Override
  public Page<LogicalMeterSummaryDto> findAll(RequestParameters parameters, Pageable pageable) {
    var logicalMeter = LogicalMeter.LOGICAL_METER;
    var physicalMeter = PhysicalMeter.PHYSICAL_METER;
    var gateway = Gateway.GATEWAY;
    var location = com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
    var meterDefinition = MeterDefinition.METER_DEFINITION;
    var meterAlarmLog = MeterAlarmLog.METER_ALARM_LOG;
    var physicalMeterStatusLog = PhysicalMeterStatusLog.PHYSICAL_METER_STATUS_LOG;

    var selectQuery = dsl.select(
      logicalMeter.ID,
      logicalMeter.ORGANISATION_ID,
      logicalMeter.EXTERNAL_ID,
      logicalMeter.CREATED,
      meterDefinition.MEDIUM,
      gateway.SERIAL,
      MISSING_MEASUREMENT_COUNT,
      physicalMeterStatusLog.STATUS,
      physicalMeter.MANUFACTURER,
      physicalMeter.ADDRESS,
      physicalMeter.READ_INTERVAL_MINUTES,
      location.LATITUDE,
      location.LONGITUDE,
      location.CONFIDENCE,
      location.COUNTRY,
      location.CITY,
      location.STREET_ADDRESS,
      meterAlarmLog.ID,
      meterAlarmLog.PHYSICAL_METER_ID,
      meterAlarmLog.START,
      meterAlarmLog.LAST_SEEN,
      meterAlarmLog.STOP,
      meterAlarmLog.MASK,
      meterAlarmLog.DESCRIPTION
    )
      // one row for each logical/physical meter, until meter replacement is implemented
      .distinctOn(logicalMeter.ID, physicalMeter.ID)
      .from(logicalMeter);

    var countQuery = dsl.select(logicalMeter.ID).from(logicalMeter);

    var filters = toFilters(parameters);

    logicalMeterJooqConditions.apply(filters, selectQuery);
    logicalMeterJooqConditions.apply(filters, countQuery);

    List<LogicalMeterSummaryDto> logicalMeters = selectQuery
      .limit(pageable.getPageSize())
      .offset(Long.valueOf(pageable.getOffset()).intValue())
      .fetchInto(LogicalMeterSummaryDto.class);

    var allMeters = parameters.getPeriod()
      .map(period -> logicalMeters.stream().map(withExpectedReadoutsFor(period)).collect(toList()))
      .orElse(logicalMeters);

    return getPage(allMeters, pageable, () -> dsl.fetchCount(countQuery));
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
        logicalMeter.UTC_OFFSET,
        location.COUNTRY,
        location.CITY,
        location.STREET_ADDRESS,
        meterDefinition.MEDIUM
      ).from(logicalMeter);

    Filters filters = toFilters(parameters);

    logicalMeterJooqConditions.apply(filters, query);

    return query.fetchInto(LogicalMeterWithLocation.class);
  }

  @Override
  public List<LogicalMeterCollectionStats> findMissingMeterReadingsCounts(
    RequestParameters parameters
  ) {
    var logicalMeter = LogicalMeter.LOGICAL_METER;
    var physicalMeter = PhysicalMeter.PHYSICAL_METER;
    var query = dsl.select(
      logicalMeter.ID,
      DSL.coalesce(MISSING_MEASUREMENT_COUNT, 0L),
      physicalMeter.READ_INTERVAL_MINUTES
    ).distinctOn(logicalMeter.ID, physicalMeter.READ_INTERVAL_MINUTES)
      .from(logicalMeter);

    Filters filters = toFilters(parameters);

    logicalMeterJooqConditions.apply(filters, query);

    return query.fetchInto(LogicalMeterCollectionStats.class);
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
      .transform(groupBy(METER_STATUS_LOG.pk.physicalMeterId).as(GroupBy.list(METER_STATUS_LOG)));
  }

  @Override
  public void delete(UUID id, UUID organisationId) {
    new JPADeleteClause(entityManager, LOGICAL_METER)
      .where(LOGICAL_METER.pk.id.eq(id)
        .and(LOGICAL_METER.pk.organisationId.eq(organisationId)))
      .execute();
  }

  private Page<String> fetchAllBy(
    RequestParameters parameters,
    Pageable pageable,
    TableField<PhysicalMeterRecord, String> field
  ) {

    var logicalMeter = LogicalMeter.LOGICAL_METER;

    var query = dsl.selectDistinct(
      field
    ).from(logicalMeter);

    var countQuery = dsl.selectDistinct(
      field
    ).from(logicalMeter);

    var filters = toFilters(parameters);
    new SelectionJooqConditions().apply(filters, query);
    new SelectionJooqConditions().apply(filters, countQuery);

    var all = query
      .orderBy(directionFor(field, pageable.getSort()))
      .limit(pageable.getPageSize())
      .offset(Long.valueOf(pageable.getOffset()).intValue())
      .fetchInto(String.class);

    return getPage(all, pageable, () -> dsl.fetchCount(countQuery));
  }

  private OrderField<String> directionFor(
    TableField<PhysicalMeterRecord, String> field,
    Sort sort
  ) {
    if (sort.isUnsorted()) {
      return field;
    }

    if (sort.iterator().next().getDirection().isAscending()) {
      return field.asc();
    } else {
      return field.desc();
    }
  }

  private Optional<LogicalMeterEntity> fetchOne(Condition... conditions) {
    var logicalMeter = LogicalMeter.LOGICAL_METER;
    var query = dsl.select()
      .from(logicalMeter)
      .where(conditions).limit(1);
    return nativeQuery(query, LogicalMeterEntity.class).stream().findAny();
  }

  private static Function<LogicalMeterSummaryDto, LogicalMeterSummaryDto> withExpectedReadoutsFor(
    SelectionPeriod period
  ) {
    return logicalMeterSummaryDto -> logicalMeterSummaryDto.toBuilder()
      .expectedReadingCount(
        Optional.ofNullable(logicalMeterSummaryDto.readIntervalMinutes)
          .map(readInterval -> expectedReadouts(readInterval, period))
          .orElse(0L))
      .build();
  }
}
