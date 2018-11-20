package com.elvaco.mvp.database.repository.jpa;

import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.repository.querydsl.LogicalMeterFilterQueryDslVisitor;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.querydsl.core.types.ExpressionUtils.allOf;
import static com.querydsl.core.types.ExpressionUtils.isNotNull;

@Repository
class SummaryQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements SummaryJpaRepository {

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
    Filters filters = toFilters(parameters);

    long meters = countMeters(filters);
    long cities = countCities(filters);
    long addresses = countAddresses(filters);

    return new MeterSummary(meters, cities, addresses);
  }

  private long countMeters(Filters filters) {
    JPQLQuery<?> query = createCountQuery();
    new LogicalMeterFilterQueryDslVisitor().visitAndApply(filters, query);
    return query.distinct().fetchCount();
  }

  private long countCities(Filters filters) {
    JPQLQuery<?> query = createQuery()
      .select(Expressions.list(LOCATION.country, LOCATION.city));
    new LogicalMeterFilterQueryDslVisitor().visitAndApply(filters, query);
    return (long) query.where(hasCity()).distinct().fetch().size();
  }

  private long countAddresses(Filters filters) {
    JPQLQuery<?> query = createQuery()
      .select(Expressions.list(LOCATION.country, LOCATION.city, LOCATION.streetAddress));
    new LogicalMeterFilterQueryDslVisitor().visitAndApply(filters, query);
    return (long) query.where(hasAddress()).distinct().fetch().size();
  }

  private static Predicate hasCity() {
    return allOf(isNotNull(LOCATION.country.lower()), isNotNull(LOCATION.city.lower()));
  }

  private static Predicate hasAddress() {
    return allOf(
      isNotNull(LOCATION.country.lower()),
      isNotNull(LOCATION.city.lower()),
      isNotNull(LOCATION.streetAddress.lower())
    );
  }
}
