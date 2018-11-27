package com.elvaco.mvp.database.repository.jpa;

import java.util.Map;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.Address;
import com.elvaco.mvp.core.domainmodels.City;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.repository.jooq.LocationJooqConditions;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.resolveSortFields;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class LocationJooqJpaRepository
  extends SimpleJpaRepository<LocationEntity, UUID>
  implements LocationJpaRepository {

  private static final Map<String, Field<String>> SORT_FIELDS_MAP = Map.of(
    "city", LOCATION.CITY,
    "streetAddress", LOCATION.STREET_ADDRESS
  );

  private final DSLContext dsl;

  @Autowired
  LocationJooqJpaRepository(EntityManager entityManager, DSLContext dsl) {
    super(LocationEntity.class, entityManager);
    this.dsl = dsl;
  }

  @Override
  public Page<City> findAllCities(RequestParameters parameters, Pageable pageable) {
    var filters = toFilters(parameters);

    var query = dsl.selectDistinct(
      LOCATION.CITY,
      LOCATION.COUNTRY
    ).from(LOCATION);

    new LocationJooqConditions().apply(filters, query);

    var addresses = query
      .where(LOCATION.COUNTRY.isNotNull().and(LOCATION.CITY.isNotNull()))
      .orderBy(resolveSortFields(parameters, SORT_FIELDS_MAP))
      .limit(pageable.getPageSize())
      .offset(Long.valueOf(pageable.getOffset()).intValue())
      .fetchInto(City.class);

    var countQuery = dsl.selectDistinct().from(LOCATION);

    new LocationJooqConditions().apply(filters, countQuery);

    return getPage(addresses, pageable, () -> dsl.fetchCount(countQuery));
  }

  @Override
  public Page<Address> findAllAddresses(RequestParameters parameters, Pageable pageable) {
    var filters = toFilters(parameters);

    var query = dsl.selectDistinct(
      LOCATION.STREET_ADDRESS,
      LOCATION.CITY,
      LOCATION.COUNTRY
    ).from(LOCATION);

    new LocationJooqConditions().apply(filters, query);

    var addresses = query
      .where(LOCATION.COUNTRY.isNotNull()
        .and(LOCATION.CITY.isNotNull())
        .and(LOCATION.STREET_ADDRESS.isNotNull()))
      .orderBy(resolveSortFields(parameters, SORT_FIELDS_MAP))
      .limit(pageable.getPageSize())
      .offset(Long.valueOf(pageable.getOffset()).intValue())
      .fetchInto(Address.class);

    var countQuery = dsl.selectDistinct().from(LOCATION);

    new LocationJooqConditions().apply(filters, countQuery);

    return getPage(addresses, pageable, () -> dsl.fetchCount(countQuery));
  }
}