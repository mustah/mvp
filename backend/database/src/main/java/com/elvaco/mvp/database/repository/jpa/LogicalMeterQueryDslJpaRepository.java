package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.queryfilters.LogicalMeterQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.MeasurementQueryFilters;
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
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.stereotype.Repository;

import static com.querydsl.core.types.ExpressionUtils.allOf;
import static com.querydsl.core.types.ExpressionUtils.isNotNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
public class LogicalMeterQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements LogicalMeterJpaRepository {

  private static final QLocationEntity LOCATION = QLocationEntity.locationEntity;
  private static final QLogicalMeterEntity LOGICAL_METER = QLogicalMeterEntity.logicalMeterEntity;

  private static final QPhysicalMeterStatusLogEntity STATUS_LOG =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  private static final QPhysicalMeterEntity PHYSICAL_METER =
    QPhysicalMeterEntity.physicalMeterEntity;

  private static final QGatewayEntity GATEWAY =
    QGatewayEntity.gatewayEntity;

  private static final QMeasurementEntity MEASUREMENT = QMeasurementEntity.measurementEntity;

  @Autowired
  public LogicalMeterQueryDslJpaRepository(EntityManager entityManager) {
    super(
      new JpaMetamodelEntityInformation<>(LogicalMeterEntity.class, entityManager.getMetamodel()),
      entityManager
    );
  }

  @Override
  public List<LogicalMeterEntity> findAll(RequestParameters parameters, Predicate predicate) {
    return applyJoins(
      createQuery(withMeasurementPredicate(parameters, predicate)).select(path),
      parameters
    ).fetch();
  }

  @Override
  public List<LogicalMeterEntity> findAll(
    RequestParameters parameters,
    Predicate predicate,
    Sort sort
  ) {
    return querydsl.applySorting(
      sort,
      applyJoins(
        createQuery(withMeasurementPredicate(parameters, predicate)).select(path),
        parameters
      )
    ).fetch();
  }

  @Override
  public Page<PagedLogicalMeter> findAll(
    RequestParameters parameters,
    Predicate predicate,
    Pageable pageable
  ) {
    predicate = withMeasurementPredicate(parameters, predicate);
    JPQLQuery<LogicalMeterEntity> countQuery = applyJoins(
      createCountQuery(predicate).select(path),
      parameters
    );

    JPQLQuery<PagedLogicalMeter> query =
      createQuery().select(Projections.constructor(
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
      )).where(predicate)
        .distinct()
        .leftJoin(LOGICAL_METER.location, LOCATION)
        .leftJoin(LOGICAL_METER.gateways, GATEWAY)
        .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
        .leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG);

    List<PagedLogicalMeter> all = querydsl.applyPagination(
      pageable,
      query
    ).fetch();

    if (!all.isEmpty()) {
      parameters.setAll(
        "id",
        all.stream().map(
          pagedLogicalMeter -> pagedLogicalMeter.id.toString()
        ).collect(toList())
      );

      all = fetchAdditionalPagedMeterData(parameters, all);
    }

    return getPage(all, pageable, countQuery::fetchCount);
  }

  @Override
  public Map<UUID, Long> findMeasurementCounts(Predicate predicate) {
    return createQuery(predicate)
      .select(MEASUREMENT)
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .join(PHYSICAL_METER.measurements, MEASUREMENT)
      .groupBy(LOGICAL_METER.id)
      .transform(
        GroupBy.groupBy(LOGICAL_METER.id).as(MEASUREMENT.count())
      );
  }

  @Override
  public List<LogicalMeterEntity> findByOrganisationId(UUID organisationId) {
    return findAll(LOGICAL_METER.organisationId.eq(organisationId));
  }

  @Override
  public Optional<LogicalMeterEntity> findById(UUID id) {
    return Optional.ofNullable(findOne(id));
  }

  @Override
  public Optional<LogicalMeterEntity> findOneBy(RequestParameters parameters) {
    return Optional.ofNullable(fetchOne(new LogicalMeterQueryFilters().toExpression(parameters)));
  }

  @Override
  public Optional<LogicalMeterEntity> findOneBy(
    UUID organisationId,
    String externalId
  ) {
    Predicate predicate = LOGICAL_METER.organisationId.eq(organisationId)
      .and(LOGICAL_METER.externalId.eq(externalId));
    return Optional.ofNullable(fetchOne(predicate));
  }

  @Override
  public MeterSummary summary(RequestParameters parameters, Predicate predicate) {
    long meters = createCountQuery(predicate)
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG)
      .distinct()
      .fetchCount();

    /*
    NOTE: we calculate .size() in Java land which causes extra memory usage.

    JQL does not support multiple distinct values ("select count(distinct a, b)..."),
    which forces us to count outside of the database.
     */
    long cities = createQuery(predicate)
      .select(Expressions.list(LOCATION.country, LOCATION.city))
      .where(
        allOf(
          isNotNull(LOCATION.country), isNotNull(LOCATION.city)
        )
      )
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG)
      .distinct()
      .fetch()
      .size();

    long addresses = createQuery(predicate)
      .select(Expressions.list(LOCATION.country, LOCATION.city, LOCATION.streetAddress))
      .where(
        allOf(
          isNotNull(LOCATION.country), isNotNull(LOCATION.city), isNotNull(LOCATION.streetAddress)
        )
      )
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG)
      .distinct()
      .fetch()
      .size();

    return new MeterSummary(meters, cities, addresses);
  }

  @Override
  public void delete(UUID id, UUID organisationId) {
    JPADeleteClause query = new JPADeleteClause(entityManager, LOGICAL_METER);
    query.where(LOGICAL_METER.id.eq(id).and(LOGICAL_METER.organisationId.eq(organisationId)))
      .execute();
  }

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
      .transform(
        GroupBy.groupBy(STATUS_LOG.physicalMeterId).as(GroupBy.list(STATUS_LOG))
      );
  }

  private Map<UUID, PhysicalMeterStatusLogEntity> findCurrentStatuses(Predicate predicate) {
    return createQuery(predicate)
      .select(STATUS_LOG.start.max())
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .join(PHYSICAL_METER.statusLogs, STATUS_LOG)
      .orderBy(STATUS_LOG.start.desc(), STATUS_LOG.stop.desc())
      .transform(
        GroupBy.groupBy(LOGICAL_METER.id).as(STATUS_LOG)
      );
  }

  private List<PagedLogicalMeter> fetchAdditionalPagedMeterData(
    RequestParameters parameters,
    List<PagedLogicalMeter> all
  ) {
    Map<UUID, Long> logicalMeterIdToMeasurementCount =
      findMeasurementCounts(new MeasurementQueryFilters()
        .toExpression(parameters));

    Map<UUID, PhysicalMeterStatusLogEntity> logicalMeterIdToCurrentStatus =
      findCurrentStatuses(new PhysicalMeterStatusLogQueryFilters()
        .toExpression(parameters));

    all = all.stream().map(pagedLogicalMeter ->
      pagedLogicalMeter
        .withMeasurementCount(logicalMeterIdToMeasurementCount.getOrDefault(
          pagedLogicalMeter.id,
          0L
        ))
        .withCurrentStatus(logicalMeterIdToCurrentStatus.getOrDefault(
          pagedLogicalMeter.id,
          null
        ))
    ).collect(toList());
    return all;
  }

  private boolean isStatusQuery(RequestParameters parameters) {
    return parameters.hasName("before")
           && parameters.hasName("after")
           && parameters.hasName("status");
  }

  private Predicate withMeasurementPredicate(RequestParameters parameters, Predicate predicate) {
    if ((parameters.hasName("minValue") || parameters.hasName("maxValue"))
        && parameters.hasName("quantity")) {

      String quantity = parameters.getFirst("quantity");

      JPAQuery<UUID> queryIds = new JPAQueryFactory(entityManager)
        .select(LOGICAL_METER.id).from(path)
        .innerJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
        .innerJoin(PHYSICAL_METER.measurements, MEASUREMENT)
        .where(
          withMeasurementAboveMax(
            parameters,
            withMeasurementBelowMin(parameters, MEASUREMENT.quantity.eq(quantity))
          )
        );

      return LOGICAL_METER.id.in(queryIds);
    } else {
      return predicate;
    }
  }

  private Predicate withMeasurementBelowMin(RequestParameters parameters, Predicate predicate) {
    if (parameters.hasName("minValue")) {
      MeasurementUnit minValue = MeasurementUnit.from(parameters.getFirst("minValue"));

      return Expressions.booleanOperation(
        Ops.LT,
        MEASUREMENT.value,
        Expressions.simpleTemplate(MeasurementUnit.class, "{0}", minValue)
      ).and(predicate);
    }

    return predicate;
  }

  private Predicate withMeasurementAboveMax(RequestParameters parameters, Predicate predicate) {
    if (parameters.hasName("maxValue")) {
      MeasurementUnit maxValue = MeasurementUnit.from(parameters.getFirst("maxValue"));

      return Expressions.booleanOperation(
        Ops.GT,
        MEASUREMENT.value,
        Expressions.simpleTemplate(MeasurementUnit.class, "{0}", maxValue)
      ).and(predicate);
    }

    return predicate;
  }

  private LogicalMeterEntity fetchOne(Predicate predicate) {
    return createQuery(predicate)
      .select(path)
      .distinct()
      .leftJoin(LOGICAL_METER.location, QLocationEntity.locationEntity)
      .fetchJoin()
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .fetchOne();
  }

  private <T> JPQLQuery<T> applyJoins(
    JPQLQuery<T> query,
    RequestParameters parameters
  ) {
    JPQLQuery<T> joinQuery = query.distinct()
      .leftJoin(LOGICAL_METER.location, QLocationEntity.locationEntity)
      .fetchJoin();

    if (isStatusQuery(parameters)) {
      joinQuery = joinQuery.leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
        .leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG);
    }

    return joinQuery;
  }
}
