package com.elvaco.mvp.database.repository.jpa;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.filter.ComparisonMode;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.filter.LocationConfidenceFilter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.repository.querydsl.GatewayFilterQueryDslJpaVisitor;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.domainmodels.GeoCoordinate.HIGH_CONFIDENCE;
import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;

@Repository
class GatewayMapQueryDslJpaRepository
  extends BaseQueryDslRepository<GatewayEntity, UUID>
  implements MapMarkerJpaRepository {

  @Autowired
  GatewayMapQueryDslJpaRepository(EntityManager entityManager) {
    super(entityManager, GatewayEntity.class);
  }

  @Override
  public Set<MapMarker> findAllMapMarkers(RequestParameters parameters) {
    JPQLQuery<MapMarker> query = createQuery()
      .select(Projections.constructor(
        MapMarker.class,
        GATEWAY.id,
        GATEWAY_STATUS_LOG.status,
        ALARM_LOG.mask,
        LOCATION.latitude,
        LOCATION.longitude
      ));

    Filters filters = toFilters(parameters);

    filters.add(new LocationConfidenceFilter(HIGH_CONFIDENCE, ComparisonMode.EQUAL));

    new GatewayFilterQueryDslJpaVisitor().visitAndApply(filters, query);

    return new HashSet<>(query.distinct().fetch());
  }
}
