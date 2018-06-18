package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PagedLogicalMeter;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.queryfilters.LogicalMeterQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.MeasurementQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.PhysicalMeterStatusLogQueryFilters;
import com.elvaco.mvp.database.util.JoinIfNeededUtil;
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

import static com.querydsl.core.group.GroupBy.groupBy;
import static java.util.stream.Collectors.toList;
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

  private static final QPhysicalMeterEntity PHYSICAL_METER =
    QPhysicalMeterEntity.physicalMeterEntity;

  private static final QGatewayEntity GATEWAY =
    QGatewayEntity.gatewayEntity;

  private static final QMeasurementEntity MEASUREMENT =
    QMeasurementEntity.measurementEntity;

  @Autowired
  LogicalMeterQueryDslJpaRepository(EntityManager entityManager) {
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

    List<PagedLogicalMeter> all = querydsl.applyPagination(pageable, query).fetch();

    if (!all.isEmpty()) {
      parameters.setAll(
        "id",
        all.stream()
          .map(pagedLogicalMeter -> pagedLogicalMeter.id.toString())
          .collect(toList())
      );
      all = fetchAdditionalPagedMeterData(parameters, all);
    }

    return getPage(all, pageable, countQuery::fetchCount);
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
  public Optional<LogicalMeterEntity> findOneBy(
    UUID organisationId,
    String externalId
  ) {
    Predicate predicate = LOGICAL_METER.organisationId.eq(organisationId)
      .and(LOGICAL_METER.externalId.eq(externalId));
    return Optional.ofNullable(fetchOne(predicate, new RequestParametersAdapter()));
  }

  @Override
  public Optional<LogicalMeterEntity> findOneBy(RequestParameters parameters) {
    return Optional.ofNullable(fetchOne(
      new LogicalMeterQueryFilters().toExpression(parameters),
      parameters
    ));
  }

  @Override
  public void delete(UUID id, UUID organisationId) {
    new JPADeleteClause(entityManager, LOGICAL_METER)
      .where(LOGICAL_METER.id.eq(id).and(LOGICAL_METER.organisationId.eq(organisationId)))
      .execute();
  }

  @Override
  public Map<UUID, Long> findMeasurementCounts(Predicate predicate) {
    return createQuery(predicate)
      .select(MEASUREMENT)
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .join(PHYSICAL_METER.measurements, MEASUREMENT)
      .groupBy(LOGICAL_METER.id)
      .transform(groupBy(LOGICAL_METER.id).as(MEASUREMENT.count()));
  }

  @SuppressWarnings(
    {"SpringDataRepositoryMethodReturnTypeInspection", "SpringDataMethodInconsistencyInspection"}
  )
  @Override
  public Map<UUID, List<PhysicalMeterStatusLogEntity>> findStatusesGroupedByPhysicalMeterId(
    Predicate predicate
  ) {
    return createQuery(predicate)
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .join(PHYSICAL_METER.statusLogs, STATUS_LOG)
      .orderBy(STATUS_LOG.start.desc(), STATUS_LOG.stop.desc())
      .transform(groupBy(STATUS_LOG.physicalMeterId).as(GroupBy.list(STATUS_LOG)));
  }

  private Map<UUID, PhysicalMeterStatusLogEntity> findCurrentStatuses(Predicate predicate) {
    return createQuery(predicate)
      .select(STATUS_LOG.start.max())
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .join(PHYSICAL_METER.statusLogs, STATUS_LOG)
      .orderBy(STATUS_LOG.start.desc(), STATUS_LOG.stop.desc())
      .transform(groupBy(LOGICAL_METER.id).as(STATUS_LOG));
  }

  private List<PagedLogicalMeter> fetchAdditionalPagedMeterData(
    RequestParameters parameters,
    List<PagedLogicalMeter> all
  ) {
    Map<UUID, Long> logicalMeterIdToMeasurementCount =
      findMeasurementCounts(new MeasurementQueryFilters().toExpression(parameters));

    Map<UUID, PhysicalMeterStatusLogEntity> logicalMeterIdToCurrentStatus =
      findCurrentStatuses(new PhysicalMeterStatusLogQueryFilters().toExpression(parameters));

    return all.stream()
      .map(pagedLogicalMeter ->
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
  }

  private Predicate withMeasurementPredicate(RequestParameters parameters, Predicate predicate) {
    if ((parameters.hasName("minValue") || parameters.hasName("maxValue"))
      && parameters.hasName("quantity")) {

      String quantity = parameters.getFirst("quantity");

      JPAQuery<UUID> queryIds = new JPAQueryFactory(entityManager)
        .select(LOGICAL_METER.id).from(path)
        .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
        .join(PHYSICAL_METER.measurements, MEASUREMENT)
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

  private LogicalMeterEntity fetchOne(Predicate predicate, RequestParameters parameters) {
    return applyJoins(createQuery(predicate).select(path), parameters).fetchOne();
  }

  private <T> JPQLQuery<T> applyJoins(JPQLQuery<T> query, RequestParameters parameters) {
    query = query.distinct()
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .fetchJoin();

    JoinIfNeededUtil.joinPhysicalMeterFromLogicalMeter(query, parameters);
    JoinIfNeededUtil.joinStatusLogsFromPhysicalMeter(query, parameters);
    JoinIfNeededUtil.joinGatewayFromLogicalMeter(query, parameters);

    return query;
  }
}
