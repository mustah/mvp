package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.Address;
import com.elvaco.mvp.core.domainmodels.City;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.repository.queryfilters.LocationQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.SortUtil;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isLocationQuery;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isOrganisationQuery;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class LocationQueryDslJpaRepository
  extends BaseQueryDslRepository<LocationEntity, UUID>
  implements LocationJpaRepository {

  @Autowired
  LocationQueryDslJpaRepository(EntityManager entityManager) {
    super(entityManager, LocationEntity.class);
  }

  @Override
  public Page<LocationEntity> findAll(RequestParameters parameters, Pageable pageable) {
    Predicate predicate = toPredicate(parameters);

    JPQLQuery<LocationEntity> query = createQuery(predicate).select(path);
    applyJoins(parameters, query);

    JPQLQuery<LocationEntity> countQuery = createCountQuery(predicate).select(path);
    applyJoins(parameters, countQuery);

    return getPage(fetchAll(pageable, query), pageable, countQuery::fetchCount);
  }

  @Override
  public Page<City> findAllCities(RequestParameters parameters, Pageable pageable) {
    Predicate predicate = toPredicate(parameters);

    JPQLQuery<City> query = allUniqueCitiesQuery(predicate);

    applyJoins(parameters, query);

    SortUtil.getSort(parameters)
      .map(sort -> querydsl.applySorting(sort, query));

    JPQLQuery<City> countQuery = allUniqueCitiesQuery(predicate);
    applyJoins(parameters, countQuery);

    return getPage(fetchAll(pageable, query), pageable, () -> (long) countQuery.fetch().size());
  }

  @Override
  public Page<Address> findAllAddresses(RequestParameters parameters, Pageable pageable) {
    Predicate predicate = toPredicate(parameters);

    JPQLQuery<Address> query = allUniqueAddressesQuery(predicate);

    applyJoins(parameters, query);

    SortUtil.getSort(parameters)
      .map(sort -> querydsl.applySorting(sort, query));

    JPQLQuery<Address> countQuery = allUniqueAddressesQuery(predicate);
    applyJoins(parameters, countQuery);

    return getPage(fetchAll(pageable, query), pageable, () -> (long) countQuery.fetch().size());
  }

  @Override
  public LocationEntity findByLogicalMeterId(UUID logicalMeterId) {
    return createQuery(LOCATION.logicalMeterId.eq(logicalMeterId)).select(path).fetchOne();
  }

  private JPQLQuery<City> allUniqueCitiesQuery(Predicate predicate) {
    return createQuery(predicate)
      .select(Projections.constructor(
        City.class,
        LOCATION.city,
        LOCATION.country
      ))
      .distinct()
      .where(
        LOCATION.city.isNotNull()
          .and(LOCATION.country.isNotNull())
      );
  }

  private JPQLQuery<Address> allUniqueAddressesQuery(Predicate predicate) {
    return createQuery(predicate)
      .select(Projections.constructor(
        Address.class,
        LOCATION.streetAddress,
        LOCATION.city,
        LOCATION.country
      ))
      .distinct()
      .where(
        LOCATION.streetAddress.isNotNull()
          .and(LOCATION.city.isNotNull())
          .and(LOCATION.country.isNotNull())
      );
  }

  private <T> List<T> fetchAll(Pageable pageable, JPQLQuery<T> query) {
    return querydsl.applyPagination(pageable, query).fetch();
  }

  private static <T> void applyJoins(RequestParameters parameters, JPQLQuery<T> query) {
    if (isOrganisationQuery(parameters) || isLocationQuery(parameters)) {
      query.innerJoin(LOCATION.logicalMeter, LOGICAL_METER);
    }
  }

  private static Predicate toPredicate(RequestParameters parameters) {
    return new LocationQueryFilters().toExpression(parameters);
  }
}
