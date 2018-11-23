package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.repository.jooq.JooqFilterVisitor;
import com.elvaco.mvp.database.repository.jooq.LogicalMeterJooqConditions;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
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
    var query = dsl.selectDistinct().from(LOGICAL_METER);

    logicalMeterJooqConditions.apply(filters, query);

    return dsl.fetchCount(query);
  }

  private long countCities(Filters filters) {
    var query = dsl.selectDistinct(LOCATION.COUNTRY, LOCATION.CITY).from(LOGICAL_METER);

    logicalMeterJooqConditions.apply(filters, query);

    return dsl.fetchCount(query
      .where(LOCATION.COUNTRY.isNotNull().and(LOCATION.CITY.isNotNull())));
  }

  private long countAddresses(Filters filters) {
    var query = dsl.selectDistinct(LOCATION.COUNTRY, LOCATION.CITY, LOCATION.STREET_ADDRESS)
      .from(LOGICAL_METER);

    logicalMeterJooqConditions.apply(filters, query);

    return dsl.fetchCount(query
      .where(LOCATION.COUNTRY.isNotNull()
        .and(LOCATION.CITY.isNotNull())
        .and(LOCATION.STREET_ADDRESS.isNotNull())));
  }
}
