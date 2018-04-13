package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.exception.PredicateConstructionFailure;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.types.Predicate;
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

  @Autowired
  public LogicalMeterQueryDslJpaRepository(EntityManager entityManager) {
    super(
      new JpaMetamodelEntityInformation<>(LogicalMeterEntity.class, entityManager.getMetamodel()),
      entityManager
    );
  }

  @Override
  public List<LogicalMeterEntity> findAll(RequestParameters parameters, Predicate predicate) {
    return queryOf(queryOfStatusPeriod(parameters, predicate)).fetch();
  }

  @Override
  public List<LogicalMeterEntity> findAll(
    RequestParameters parameters,
    Predicate predicate,
    Sort sort
  ) {
    return querydsl.applySorting(sort, queryOf(queryOfStatusPeriod(parameters, predicate))).fetch();
  }

  @Override
  public Page<LogicalMeterEntity> findAll(
    RequestParameters parameters,
    Predicate predicate,
    Pageable pageable
  ) {
    Predicate predicateWithStatusPeriod = queryOfStatusPeriod(parameters, predicate);

    JPQLQuery<LogicalMeterEntity> countQuery = createCountQuery(predicateWithStatusPeriod)
      .select(path);

    List<LogicalMeterEntity> all = querydsl.applyPagination(
      pageable,
      createQuery(predicateWithStatusPeriod).select(path)
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
