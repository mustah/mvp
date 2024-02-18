package com.elvaco.mvp.database.repository.jpa;

import java.util.Map;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.Address;
import com.elvaco.mvp.core.domainmodels.City;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.repository.jooq.FilterVisitors;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record3;
import org.jooq.Record5;
import org.jooq.SelectForUpdateStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.levenshtein;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.resolveSortFields;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class LocationJooqJpaRepository
  extends SimpleJpaRepository<LocationEntity, EntityPk>
  implements LocationJpaRepository {

  private static final Map<String, Field<?>> SORT_FIELDS_MAP = Map.of(
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
    Field<Integer> editDistance = levenshtein(
      LOCATION.CITY,
      parameters.getFirst(RequestParameter.Q_CITY)
    );

    var query = dsl.selectDistinct(
      LOCATION.CITY,
      LOCATION.COUNTRY,
      editDistance
    ).from(LOCATION);

    var countQuery = dsl.selectDistinct().from(LOCATION);

    FilterVisitors.location().accept(toFilters(parameters))
      .andJoinsOn(query)
      .andJoinsOn(countQuery);

    SelectForUpdateStep<Record3<String, String, Integer>> select = query
      .where(LOCATION.COUNTRY.isNotNull().and(LOCATION.CITY.isNotNull()))
      .orderBy(resolveSortFields(parameters, SORT_FIELDS_MAP, editDistance.asc()))
      .limit(pageable.getPageSize())
      .offset((int) pageable.getOffset());

    var addresses = select.fetch()
      .stream()
      .map(record -> new City(record.value1(), record.value2()))
      .toList();

    return getPage(addresses, pageable, () -> dsl.fetchCount(countQuery));
  }

  @Override
  public Page<Address> findAllAddresses(RequestParameters parameters, Pageable pageable) {
    Field<Integer> editDistance = levenshtein(
      LOCATION.STREET_ADDRESS,
      parameters.getFirst(RequestParameter.Q_ADDRESS)
    );
    var query = dsl.selectDistinct(
      LOCATION.STREET_ADDRESS,
      LOCATION.ZIP,
      LOCATION.CITY,
      LOCATION.COUNTRY,
      editDistance
    ).from(LOCATION);

    var countQuery = dsl.selectDistinct().from(LOCATION);

    FilterVisitors.location().accept(toFilters(parameters))
      .andJoinsOn(query)
      .andJoinsOn(countQuery);

    SelectForUpdateStep<Record5<String, String, String, String, Integer>> select = query
      .where(LOCATION.COUNTRY.isNotNull()
        .and(LOCATION.CITY.isNotNull())
        .and(LOCATION.STREET_ADDRESS.isNotNull()))
      .orderBy(resolveSortFields(parameters, SORT_FIELDS_MAP, editDistance.asc()))
      .limit(pageable.getPageSize())
      .offset((int) pageable.getOffset());

    var addresses = select.fetch().stream()
      .map(record -> new Address(record.value1(), record.value2(), record.value3(), record.value4())
      ).toList();

    return getPage(addresses, pageable, () -> dsl.fetchCount(countQuery));
  }
}
