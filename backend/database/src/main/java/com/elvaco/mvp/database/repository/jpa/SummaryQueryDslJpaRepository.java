package com.elvaco.mvp.database.repository.jpa;

import java.util.UUID;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.queryfilters.LogicalMeterQueryFilters;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.meter.QLocationEntity.locationEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity.physicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isLocationQuery;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinLogicalMeterGateways;
import static com.querydsl.core.types.ExpressionUtils.allOf;
import static com.querydsl.core.types.ExpressionUtils.isNotNull;

@Repository
class SummaryQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements SummaryJpaRepository {

  private static final QLocationEntity LOCATION = locationEntity;

  private static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;

  private static final QPhysicalMeterStatusLogEntity STATUS_LOG = physicalMeterStatusLogEntity;

  private static final QPhysicalMeterEntity PHYSICAL_METER = physicalMeterEntity;

  @Autowired
  SummaryQueryDslJpaRepository(EntityManager entityManager) {
    super(entityManager, LogicalMeterEntity.class);
  }

  /**
   * NOTE: we calculate .size() in Java land which causes extra memory usage.
   * JQL does not support multiple distinct values ("select count(distinct a, b)..."),
   * which forces us to count outside of the database.
   */
  @Override
  public MeterSummary summary(RequestParameters parameters) {
    Predicate predicate = toPredicate(parameters);

    long meters = countMeters(parameters, predicate);
    long cities = countCities(parameters, predicate);
    long addresses = countAddresses(parameters, predicate);

    return new MeterSummary(meters, cities, addresses);
  }

  private long countMeters(RequestParameters parameters, Predicate predicate) {
    JPQLQuery<?> query = createCountQuery(predicate);

    applyJoins(query, parameters);

    return query.distinct().fetchCount();
  }

  private long countCities(RequestParameters parameters, Predicate predicate) {
    JPQLQuery<?> query = createQuery(predicate)
      .select(Expressions.list(LOCATION.country, LOCATION.city))
      .where(hasCity(parameters));

    applyJoins(query, parameters);

    return (long) query.distinct().fetch().size();
  }

  private long countAddresses(RequestParameters parameters, Predicate predicate) {
    JPQLQuery<?> query = createQuery(predicate)
      .select(Expressions.list(LOCATION.country, LOCATION.city, LOCATION.streetAddress))
      .where(hasAddress(parameters));

    applyJoins(query, parameters);

    return (long) query.distinct().fetch().size();
  }

  private static JPQLQuery<?> applyJoins(JPQLQuery<?> query, RequestParameters parameters) {
    query
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG);

    joinLogicalMeterGateways(query, parameters);

    return query;
  }

  @Nullable
  private static Predicate hasCity(RequestParameters parameters) {
    return isLocationQuery(parameters)
      ? null
      : allOf(isNotNull(LOCATION.country), isNotNull(LOCATION.city));
  }

  private static Predicate hasAddress(RequestParameters parameters) {
    return isLocationQuery(parameters)
      ? null
      : allOf(
        isNotNull(LOCATION.country),
        isNotNull(LOCATION.city),
        isNotNull(LOCATION.streetAddress)
      );
  }

  private static Predicate toPredicate(RequestParameters parameters) {
    return new LogicalMeterQueryFilters().toExpression(parameters);
  }
}
