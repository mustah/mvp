package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.exception.PredicateConstructionFailure;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.stereotype.Repository;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
public class LogicalMeterQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements LogicalMeterJpaRepository {

  private static final QLogicalMeterEntity LOGICAL_METER = QLogicalMeterEntity.logicalMeterEntity;

  private static final QPhysicalMeterStatusLogEntity STATUS_LOG =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  private static final QPhysicalMeterEntity PHYSICAL_METER =
    QPhysicalMeterEntity.physicalMeterEntity;

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
    return queryOf(queryOfStatusPeriod(
      parameters,
      queryOfMeasurement(parameters, predicate)
    )).fetch();
  }

  @Override
  public List<LogicalMeterEntity> findAll(
    RequestParameters parameters,
    Predicate predicate,
    Sort sort
  ) {
    return querydsl.applySorting(
      sort,
      queryOf(queryOfStatusPeriod(
        parameters,
        queryOfMeasurement(parameters, predicate)
      ))
    ).fetch();
  }

  @Override
  public Page<LogicalMeterEntity> findAll(
    RequestParameters parameters,
    Predicate predicate,
    Pageable pageable
  ) {
    Predicate withStatusPeriodAndMeasurement = queryOfMeasurement(
      parameters,
      queryOfStatusPeriod(
        parameters,
        queryOfMeasurement(parameters, predicate)
      )
    );

    JPQLQuery<LogicalMeterEntity> countQuery = createCountQuery(withStatusPeriodAndMeasurement)
      .select(path);

    List<LogicalMeterEntity> all = querydsl.applyPagination(
      pageable,
      createQuery(withStatusPeriodAndMeasurement).select(path)
    ).fetch();

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
  public Optional<LogicalMeterEntity> findBy(
    UUID organisationId,
    String externalId
  ) {
    Predicate predicate = LOGICAL_METER.organisationId.eq(organisationId)
      .and(LOGICAL_METER.externalId.eq(externalId));
    return Optional.ofNullable(fetchOne(predicate));
  }

  @Override
  public Optional<LogicalMeterEntity> findBy(
    UUID organisationId,
    UUID id
  ) {
    Predicate predicate = LOGICAL_METER.organisationId.eq(organisationId)
      .and(LOGICAL_METER.id.eq(id));
    return Optional.ofNullable(fetchOne(predicate));
  }

  private Predicate queryOfStatusPeriod(RequestParameters parameters, Predicate predicate) {
    JPAQuery<UUID> queryIds = new JPAQueryFactory(entityManager)
      .select(LOGICAL_METER.id).from(path)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG)
      .where(withStatusWithinPeriod(parameters, predicate));

    return LOGICAL_METER.id.in(queryIds);
  }

  private Predicate withStatusWithinPeriod(RequestParameters parameters, Predicate predicate) {
    if (parameters.hasName("before")
        && parameters.hasName("after")
        && parameters.hasName("status")) {
      ZonedDateTime start = parseDateParam(parameters, "after");
      ZonedDateTime stop = parseDateParam(parameters, "before");
      List<StatusType> statuses = parameters.getValues("status")
        .stream()
        .map(StatusType::from)
        .collect(toList());

      return (
        (STATUS_LOG.stop.isNull().or(STATUS_LOG.stop.after(start)))
          .and(
            STATUS_LOG.start.before(stop)
          ).and(STATUS_LOG.status.in(statuses)))
        .and(predicate);
    } else {
      return predicate;
    }
  }

  private Predicate queryOfMeasurement(RequestParameters parameters, Predicate predicate) {
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

  private ZonedDateTime parseDateParam(RequestParameters parameters, String name) {
    try {
      return ZonedDateTime.parse(parameters.getFirst(name));
    } catch (Exception exception) {
      throw new PredicateConstructionFailure(name, parameters.getValues(name), exception);
    }
  }

  private LogicalMeterEntity fetchOne(Predicate predicate) {
    return queryOf(predicate).fetchOne();
  }

  private JPQLQuery<LogicalMeterEntity> queryOf(Predicate predicate) {
    return createQuery(predicate)
      .select(path)
      .leftJoin(LOGICAL_METER.location, QLocationEntity.locationEntity)
      .fetchJoin();
  }
}
