package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.database.repository.jooq.FilterAcceptor;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.tables.LogicalMeter.LOGICAL_METER;

@RequiredArgsConstructor
@Repository
class SummaryJooqJpaRepository implements SummaryJpaRepository {

  private final DSLContext dsl;
  private final FilterAcceptor logicalMeterFilters;

  @Override
  public long meterCount(Filters filters) {
    var query = dsl.select(DSL.countDistinct(LOGICAL_METER.ID)).from(LOGICAL_METER);

    logicalMeterFilters.accept(filters).andJoinsOn(query);

    return query.fetchOne(0, Long.class);
  }

  @Override
  public long cityCount(Filters filters) {
    var query = dsl.select(DSL.countDistinct(LOCATION.COUNTRY, LOCATION.CITY)).from(LOGICAL_METER);

    logicalMeterFilters.accept(filters).andJoinsOn(query);

    return query.where(LOCATION.COUNTRY.isNotNull().and(LOCATION.CITY.isNotNull()))
      .fetchOne(0, Long.class);
  }

  @Override
  public long addressCount(Filters filters) {
    var query = dsl.select(DSL.countDistinct(
      LOCATION.COUNTRY,
      LOCATION.CITY,
      LOCATION.STREET_ADDRESS
    )).from(LOGICAL_METER);

    logicalMeterFilters.accept(filters).andJoinsOn(query);

    return query.where(LOCATION.COUNTRY.isNotNull()
      .and(LOCATION.CITY.isNotNull())
      .and(LOCATION.STREET_ADDRESS.isNotNull()))
      .fetchOne(0, Long.class);
  }
}
