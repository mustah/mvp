package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.repository.jooq.JooqFilterVisitor;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.tables.LogicalMeter.LOGICAL_METER;

@RequiredArgsConstructor
@Repository
class SummaryJooqJpaRepository implements SummaryJpaRepository {

  private final DSLContext dsl;
  private final JooqFilterVisitor logicalMeterJooqConditions;

  @Override
  public MeterSummary summary(RequestParameters parameters) {
    Filters filters = toFilters(parameters);

    long meters = countMeters(filters);
    long cities = countCities(filters);
    long addresses = countAddresses(filters);

    return new MeterSummary(meters, cities, addresses);
  }

  private long countMeters(Filters filters) {
    var query = dsl.select(DSL.countDistinct(LOGICAL_METER.ID)).from(LOGICAL_METER);

    logicalMeterJooqConditions.apply(filters, query);
    return query.fetchOne(0, Long.class);
  }

  private Long countCities(Filters filters) {
    var query = dsl.select(DSL.countDistinct(LOCATION.COUNTRY, LOCATION.CITY)).from(LOGICAL_METER);

    logicalMeterJooqConditions.apply(filters, query);

    return query.where(LOCATION.COUNTRY.isNotNull().and(LOCATION.CITY.isNotNull()))
      .fetchOne(0, Long.class);
  }

  private Long countAddresses(Filters filters) {
    var query = dsl.select(DSL.countDistinct(
      LOCATION.COUNTRY,
      LOCATION.CITY,
      LOCATION.STREET_ADDRESS
    )).from(LOGICAL_METER);

    logicalMeterJooqConditions.apply(filters, query);

    return query
      .where(LOCATION.COUNTRY.isNotNull()
        .and(LOCATION.CITY.isNotNull())
        .and(LOCATION.STREET_ADDRESS.isNotNull()))
      .fetchOne(0, Long.class);
  }
}
