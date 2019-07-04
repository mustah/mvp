package com.elvaco.mvp.database.repository.jpa;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.DisplayMode;
import com.elvaco.mvp.core.domainmodels.QuantityParameter;
import com.elvaco.mvp.core.dto.LegendDto;
import com.elvaco.mvp.core.dto.LogicalMeterSummaryDto;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.jooq.Tables;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.repository.jooq.FilterAcceptor;
import com.elvaco.mvp.database.repository.jooq.FilterVisitors;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.SelectForUpdateStep;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.elvaco.mvp.database.entity.jooq.Tables.DISPLAY_QUANTITY;
import static com.elvaco.mvp.database.entity.jooq.Tables.MEDIUM;
import static com.elvaco.mvp.database.entity.jooq.Tables.QUANTITY;
import static com.elvaco.mvp.database.entity.jooq.tables.Gateway.GATEWAY;
import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.tables.LogicalMeter.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.MeterAlarmLog.METER_ALARM_LOG;
import static com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeter.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeterStatusLog.PHYSICAL_METER_STATUS_LOG;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.COLLECTION_PERCENTAGE;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.periodContains;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.levenshtein;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.resolveSortFields;
import static java.util.stream.Collectors.toList;
import static org.jooq.impl.DSL.noCondition;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class LogicalMeterJooqJpaRepository
  extends BaseJooqRepository<LogicalMeterEntity, UUID>
  implements LogicalMeterJpaRepository {

  private static final Map<String, Field<?>> SORT_FIELDS_MAP = Map.of(
    "facility", LOGICAL_METER.EXTERNAL_ID,
    "streetAddress", LOCATION.STREET_ADDRESS.lower(),
    "city", LOCATION.CITY.lower(),
    "manufacturer", PHYSICAL_METER.MANUFACTURER,
    "gatewaySerial", GATEWAY.SERIAL,
    "secondaryAddress", PHYSICAL_METER.ADDRESS,
    "medium", MEDIUM.NAME,
    "reported", PHYSICAL_METER_STATUS_LOG.STATUS,
    "alarm", METER_ALARM_LOG.MASK
  );

  private final DSLContext dsl;
  private final FilterAcceptor logicalMeterFilters;
  private final FilterAcceptor displayQuantityFilters;

  @Autowired
  LogicalMeterJooqJpaRepository(
    EntityManager entityManager,
    DSLContext dsl,
    FilterAcceptor logicalMeterFilters,
    FilterAcceptor displayQuantityFilters
  ) {
    super(entityManager, LogicalMeterEntity.class);
    this.dsl = dsl;
    this.logicalMeterFilters = logicalMeterFilters;
    this.displayQuantityFilters = displayQuantityFilters;
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
  public Page<String> findSecondaryAddresses(RequestParameters parameters, Pageable pageable) {
    return fetchAllBy(parameters, pageable, PHYSICAL_METER.ADDRESS,
      periodContains(Tables.PHYSICAL_METER.ACTIVE_PERIOD, OffsetDateTime.now())
    );
  }

  @Override
  public Page<String> findFacilities(RequestParameters parameters, Pageable pageable) {
    return fetchAllBy(parameters, pageable, LOGICAL_METER.EXTERNAL_ID, noCondition());
  }

  @Override
  public List<LegendDto> findAllLegendItems(RequestParameters parameters, Pageable pageable) {
    return findAll(parameters, pageable)
      .getContent().stream()
      .map(it -> new LegendDto(it.id, it.externalId, it.medium))
      .collect(toList());
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
      MEDIUM.NAME,
      GATEWAY.SERIAL,
      DSL.field("null", Double.class).as(COLLECTION_PERCENTAGE.getName()), // TODO remove
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
      METER_ALARM_LOG.MASK
    ).from(LOGICAL_METER);

    var countQuery = dsl.selectDistinct(LOGICAL_METER.ID, PHYSICAL_METER.ID).from(LOGICAL_METER);

    logicalMeterFilters.accept(toFilters(parameters))
      .andJoinsOn(selectQuery)
      .andJoinsOn(countQuery);

    SelectForUpdateStep<Record> select = selectQuery
      .orderBy(resolveSortFields(parameters, SORT_FIELDS_MAP, LOGICAL_METER.EXTERNAL_ID.asc()))
      .limit(pageable.getPageSize())
      .offset(Long.valueOf(pageable.getOffset()).intValue());

    List<LogicalMeterSummaryDto> logicalMeters = select
      .fetchInto(LogicalMeterSummaryDto.class);

    return getPage(logicalMeters, pageable, () -> dsl.fetchCount(countQuery));
  }

  @Override
  public void delete(UUID id, UUID organisationId) {
    dsl.deleteFrom(LOGICAL_METER)
      .where(LOGICAL_METER.ID.eq(id))
      .and(LOGICAL_METER.ORGANISATION_ID.eq(organisationId))
      .execute();
  }

  @Override
  public void changeMeterDefinition(
    UUID organisationId,
    Long fromMeterDefinitionId,
    Long toMeterDefinitionId
  ) {
    dsl.update(LOGICAL_METER)
      .set(LOGICAL_METER.METER_DEFINITION_ID, toMeterDefinitionId)
      .where(LOGICAL_METER.ORGANISATION_ID.eq(organisationId))
      .and(LOGICAL_METER.METER_DEFINITION_ID.eq(fromMeterDefinitionId))
      .execute();
  }

  @Override
  public List<QuantityParameter> getPreferredQuantityParameters(RequestParameters parameters) {
    Field<Long> cnt = DSL.field("cnt", Long.class);

    var query = dsl.select(
      DSL.count(QUANTITY.NAME).as(cnt),
      QUANTITY.NAME,
      DISPLAY_QUANTITY.DISPLAY_UNIT,
      DISPLAY_QUANTITY.DISPLAY_MODE
    ).distinctOn(QUANTITY.NAME)
      .from(LOGICAL_METER);

    displayQuantityFilters.accept((toFilters(parameters))).andJoinsOn(query);

    var orderedQuery = query.groupBy(
      QUANTITY.NAME,
      DISPLAY_QUANTITY.DISPLAY_UNIT,
      DISPLAY_QUANTITY.DISPLAY_MODE
    ).orderBy(
      QUANTITY.NAME,
      cnt.desc(),
      DISPLAY_QUANTITY.DISPLAY_UNIT,
      DISPLAY_QUANTITY.DISPLAY_MODE
    );

    return orderedQuery.fetch().stream()
      .map(record -> new QuantityParameter(
        record.value2(),
        record.value3(),
        DisplayMode.from(record.value4())
      )).collect(toList());
  }

  private Page<String> fetchAllBy(
    RequestParameters parameters,
    Pageable pageable,
    TableField<?, String> field,
    Condition whereCondition
  ) {
    Field<Integer> editDistance = levenshtein(
      field,
      parameters.getFirst(
        RequestParameter.Q_FACILITY,
        RequestParameter.Q_SECONDARY_ADDRESS
      )
    );

    var selectQuery = dsl.selectDistinct(field, editDistance).from(LOGICAL_METER);
    var countQuery = dsl.selectDistinct(field).from(LOGICAL_METER);

    FilterVisitors.selection().accept(toFilters(parameters))
      .andJoinsOn(selectQuery)
      .andJoinsOn(countQuery);

    var select = selectQuery.where(whereCondition)
      .orderBy(orderOf(field, pageable.getSort(), editDistance.asc()))
      .limit(pageable.getPageSize())
      .offset(Long.valueOf(pageable.getOffset()).intValue());

    var all = select.fetch().stream()
      .map(Record2::value1)
      .collect(toList());

    return getPage(all, pageable, () -> dsl.fetchCount(countQuery));
  }

  private OrderField<?> orderOf(
    TableField<?, String> field,
    Sort sort,
    OrderField<?> defaultOrder
  ) {
    if (sort.isUnsorted()) {
      return defaultOrder;
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
}
