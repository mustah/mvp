package com.elvaco.mvp.database.repository.jpa;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.jooq.tables.records.PhysicalMeterRecord;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterWithLocation;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.jooq.FilterAcceptor;
import com.elvaco.mvp.database.repository.jooq.FilterVisitors;
import com.elvaco.mvp.database.repository.queryfilters.PhysicalMeterStatusLogQueryFilters;

import com.querydsl.core.group.GroupBy;
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
import static com.elvaco.mvp.database.entity.jooq.tables.Gateway.GATEWAY;
import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.tables.LogicalMeter.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.MeterAlarmLog.METER_ALARM_LOG;
import static com.elvaco.mvp.database.entity.jooq.tables.MeterDefinition.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeter.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeterStatusLog.PHYSICAL_METER_STATUS_LOG;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity.physicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.MISSING_MEASUREMENT_COUNT;
import static com.querydsl.core.group.GroupBy.groupBy;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class LogicalMeterQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements LogicalMeterJpaRepository {

  private final DSLContext dsl;
  private final FilterAcceptor logicalMeterFilters;

  @Autowired
  LogicalMeterQueryDslJpaRepository(
    EntityManager entityManager,
    DSLContext dsl,
    FilterAcceptor logicalMeterFilters
  ) {
    super(entityManager, LogicalMeterEntity.class);
    this.dsl = dsl;
    this.logicalMeterFilters = logicalMeterFilters;
  }

  @Override
  public Optional<LogicalMeterEntity> findById(UUID id) {
    return fetchOne(LOGICAL_METER.ID.equal(id));
  }

  @Override
  public Optional<LogicalMeterEntity> findByPrimaryKey(UUID organisationId, UUID id) {
    Condition condition = LOGICAL_METER.ORGANISATION_ID.equal(organisationId)
      .and(LOGICAL_METER.ID.equal(id));
    return fetchOne(condition);
  }

  @Override
  public Optional<LogicalMeterEntity> findBy(UUID organisationId, String externalId) {
    Condition condition = LOGICAL_METER.ORGANISATION_ID.equal(organisationId)
      .and(LOGICAL_METER.EXTERNAL_ID.equal(externalId));
    return fetchOne(condition);
  }

  @Override
  public Optional<LogicalMeterEntity> findBy(RequestParameters parameters) {
    SelectJoinStep<Record> query = dsl.select().from(LOGICAL_METER);

    logicalMeterFilters.apply(toFilters(parameters)).applyJoinsOn(query);

    String sql = query.getSQL(ParamType.NAMED);
    Query nativeQuery = entityManager.createNativeQuery(sql, LogicalMeterEntity.class);

    query.getParams().forEach(nativeQuery::setParameter);

    return Optional.ofNullable((LogicalMeterEntity) nativeQuery.getSingleResult());
  }

  @Override
  public Page<String> findSecondaryAddresses(RequestParameters parameters, Pageable pageable) {
    return fetchAllBy(parameters, pageable, PHYSICAL_METER.ADDRESS);
  }

  @Override
  public Page<String> findFacilities(RequestParameters parameters, Pageable pageable) {
    return fetchAllBy(parameters, pageable, PHYSICAL_METER.EXTERNAL_ID);
  }

  @Override
  public List<LogicalMeterEntity> findByOrganisationId(UUID organisationId) {
    return nativeQuery(dsl.select().from(LOGICAL_METER)
      .where(LOGICAL_METER.ORGANISATION_ID.equal(organisationId)));
  }

  @Override
  public List<LogicalMeterEntity> findAll(RequestParameters parameters) {
    var query = dsl.select().from(LOGICAL_METER);

    logicalMeterFilters.apply(toFilters(parameters)).applyJoinsOn(query);

    return nativeQuery(query);
  }

  @Override
  public Page<LogicalMeterSummaryDto> findAll(RequestParameters parameters, Pageable pageable) {
    var selectQuery = dsl.select(
      LOGICAL_METER.ID,
      LOGICAL_METER.ORGANISATION_ID,
      LOGICAL_METER.EXTERNAL_ID,
      LOGICAL_METER.CREATED,
      METER_DEFINITION.MEDIUM,
      GATEWAY.SERIAL,
      MISSING_MEASUREMENT_COUNT,
      PHYSICAL_METER_STATUS_LOG.STATUS,
      PHYSICAL_METER.MANUFACTURER,
      PHYSICAL_METER.ADDRESS,
      PHYSICAL_METER.READ_INTERVAL_MINUTES,
      LOCATION.LATITUDE,
      LOCATION.LONGITUDE,
      LOCATION.CONFIDENCE,
      LOCATION.COUNTRY,
      LOCATION.CITY,
      LOCATION.STREET_ADDRESS,
      METER_ALARM_LOG.ID,
      METER_ALARM_LOG.PHYSICAL_METER_ID,
      METER_ALARM_LOG.START,
      METER_ALARM_LOG.LAST_SEEN,
      METER_ALARM_LOG.STOP,
      METER_ALARM_LOG.MASK,
      METER_ALARM_LOG.DESCRIPTION
    ).from(LOGICAL_METER);

    var countQuery = dsl.selectDistinct(LOGICAL_METER.ID, PHYSICAL_METER.ID).from(LOGICAL_METER);

    logicalMeterFilters.apply(toFilters(parameters))
      .applyJoinsOn(selectQuery)
      .applyJoinsOn(countQuery);

    List<LogicalMeterSummaryDto> logicalMeters = selectQuery
      .limit(pageable.getPageSize())
      .offset(Long.valueOf(pageable.getOffset()).intValue())
      .fetchInto(LogicalMeterSummaryDto.class);

    List<LogicalMeterSummaryDto> allMeters = parameters.getPeriod()
      .map(period -> logicalMeters.stream().map(withExpectedReadoutsFor(period)).collect(toList()))
      .orElse(logicalMeters);

    return getPage(allMeters, pageable, () -> dsl.fetchCount(countQuery));
  }

  @Override
  public Set<LogicalMeterWithLocation> findAllForSelectionTree(RequestParameters parameters) {
    var query = dsl.select(
      LOGICAL_METER.ID,
      LOGICAL_METER.ORGANISATION_ID,
      LOGICAL_METER.EXTERNAL_ID,
      LOGICAL_METER.UTC_OFFSET,
      LOCATION.COUNTRY,
      LOCATION.CITY,
      LOCATION.STREET_ADDRESS,
      METER_DEFINITION.MEDIUM
    ).from(LOGICAL_METER);

    logicalMeterFilters.apply(toFilters(parameters)).applyJoinsOn(query);

    return new HashSet<>(query.fetchInto(LogicalMeterWithLocation.class));
  }

  @Override
  public List<LogicalMeterCollectionStats> findMissingMeterReadingsCounts(
    RequestParameters parameters
  ) {
    var query = dsl.select(
      LOGICAL_METER.ID,
      DSL.coalesce(MISSING_MEASUREMENT_COUNT, 0L),
      PHYSICAL_METER.READ_INTERVAL_MINUTES
    ).distinctOn(LOGICAL_METER.ID, PHYSICAL_METER.READ_INTERVAL_MINUTES)
      .from(LOGICAL_METER);

    logicalMeterFilters.apply(toFilters(parameters)).applyJoinsOn(query);

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
      .join(logicalMeterEntity.physicalMeters, physicalMeterEntity)
      .join(physicalMeterEntity.statusLogs, physicalMeterStatusLogEntity)
      .orderBy(physicalMeterStatusLogEntity.start.desc(), physicalMeterStatusLogEntity.stop.desc())
      .transform(groupBy(physicalMeterStatusLogEntity.pk.physicalMeterId).as(GroupBy.list(
        physicalMeterStatusLogEntity)));
  }

  @Override
  public void delete(UUID id, UUID organisationId) {
    new JPADeleteClause(entityManager, logicalMeterEntity)
      .where(logicalMeterEntity.pk.id.eq(id)
        .and(logicalMeterEntity.pk.organisationId.eq(organisationId)))
      .execute();
  }

  private Page<String> fetchAllBy(
    RequestParameters parameters,
    Pageable pageable,
    TableField<PhysicalMeterRecord, String> field
  ) {
    var selectQuery = dsl.selectDistinct(field).from(LOGICAL_METER);
    var countQuery = dsl.selectDistinct(field).from(LOGICAL_METER);

    FilterVisitors.selection().apply(toFilters(parameters))
      .applyJoinsOn(selectQuery)
      .applyJoinsOn(countQuery);

    var all = selectQuery
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
    } else if (sort.iterator().next().getDirection().isAscending()) {
      return field.asc();
    } else {
      return field.desc();
    }
  }

  private Optional<LogicalMeterEntity> fetchOne(Condition... conditions) {
    return nativeQuery(dsl.select().from(LOGICAL_METER).where(conditions).limit(1)).stream()
      .findAny();
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
