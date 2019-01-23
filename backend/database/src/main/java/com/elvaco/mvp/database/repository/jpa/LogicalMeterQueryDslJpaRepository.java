package com.elvaco.mvp.database.repository.jpa;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.jooq.tables.records.PhysicalMeterRecord;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterWithLocation;
import com.elvaco.mvp.database.repository.jooq.FilterAcceptor;
import com.elvaco.mvp.database.repository.jooq.FilterVisitors;

import com.querydsl.jpa.impl.JPADeleteClause;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.SelectForUpdateStep;
import org.jooq.SelectJoinStep;
import org.jooq.SortField;
import org.jooq.TableField;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.elvaco.mvp.database.entity.jooq.tables.Gateway.GATEWAY;
import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.tables.LogicalMeter.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.MeterAlarmLog.METER_ALARM_LOG;
import static com.elvaco.mvp.database.entity.jooq.tables.MeterDefinition.METER_DEFINITION;
import static com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeter.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeterStatusLog.PHYSICAL_METER_STATUS_LOG;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.COLLECTION_PERCENTAGE;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.resolveSortFields;
import static org.jooq.impl.DSL.field;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Slf4j
@Repository
class LogicalMeterQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements LogicalMeterJpaRepository {

  private static final Map<String, Field<?>> SORT_FIELDS_MAP = Map.of(
    "facility", LOGICAL_METER.EXTERNAL_ID,
    "address", LOCATION.STREET_ADDRESS,
    "city", LOCATION.CITY,
    "manufacturer", PHYSICAL_METER.MANUFACTURER,
    "gatewaySerial", GATEWAY.SERIAL,
    "secondaryAddress", PHYSICAL_METER.ADDRESS,
    "medium", METER_DEFINITION.MEDIUM,
    "collectionPercentage", COLLECTION_PERCENTAGE
  );

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

    logicalMeterFilters.accept(toFilters(parameters)).andJoinsOn(query);

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
  public Set<LogicalMeterEntity> findAll(RequestParameters parameters) {
    var query = dsl.select().from(LOGICAL_METER);

    logicalMeterFilters.accept(toFilters(parameters)).andJoinsOn(query);

    return new HashSet<>(nativeQuery(query));
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
      COLLECTION_PERCENTAGE,
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
      LOCATION.ZIP,
      METER_ALARM_LOG.ID,
      METER_ALARM_LOG.PHYSICAL_METER_ID,
      METER_ALARM_LOG.START,
      METER_ALARM_LOG.LAST_SEEN,
      METER_ALARM_LOG.STOP,
      METER_ALARM_LOG.MASK,
      METER_ALARM_LOG.DESCRIPTION
    ).from(LOGICAL_METER);

    var countQuery = dsl.selectDistinct(LOGICAL_METER.ID, PHYSICAL_METER.ID).from(LOGICAL_METER);

    logicalMeterFilters.accept(toFilters(parameters))
      .andJoinsOn(selectQuery)
      .andJoinsOn(countQuery);

    Collection<SortField<?>> sortFields = resolveSortFields(parameters, SORT_FIELDS_MAP);
    if (sortFields.isEmpty()) {
      sortFields.add(LOGICAL_METER.EXTERNAL_ID.asc());
    }
    SelectForUpdateStep<Record> select = selectQuery
      .orderBy(sortFields)
      .limit(pageable.getPageSize())
      .offset(Long.valueOf(pageable.getOffset()).intValue());

    List<LogicalMeterSummaryDto> logicalMeters = select
      .fetchInto(LogicalMeterSummaryDto.class);

    return getPage(logicalMeters, pageable, () -> dsl.fetchCount(countQuery));
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
      LOCATION.ZIP,
      METER_DEFINITION.MEDIUM
    ).from(LOGICAL_METER);

    logicalMeterFilters.accept(toFilters(parameters)).andJoinsOn(query);

    return new HashSet<>(query.fetchInto(LogicalMeterWithLocation.class));
  }

  @Override
  public List<LogicalMeterCollectionStats> findMeterCollectionStats(
    RequestParameters parameters
  ) {
    var query = dsl.select(
      LOGICAL_METER.ID,
      DSL.coalesce(COLLECTION_PERCENTAGE, 0)
    ).distinctOn(LOGICAL_METER.ID)
      .from(LOGICAL_METER);

    logicalMeterFilters.accept(toFilters(parameters)).andJoinsOn(query);

    return query.fetchInto(LogicalMeterCollectionStats.class);
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

    FilterVisitors.selection().accept(toFilters(parameters))
      .andJoinsOn(selectQuery)
      .andJoinsOn(countQuery);

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

  private static SortField<?> getSortField(Sort sort) {
    Iterator<Sort.Order> orderIterator = sort.iterator();
    Sort.Order order = orderIterator.next(); //only consider first sort for now
    if (orderIterator.hasNext()) {
      log.warn("Sorting on more than one property requested in: '%'", sort);
    }
    Field<?> sortField = field(order.getProperty());

    return order.getDirection().isAscending() ? sortField.asc() : sortField.desc();
  }
}
