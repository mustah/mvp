package com.elvaco.mvp.database.repository.jpa;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.filter.ComparisonMode;
import com.elvaco.mvp.core.filter.FilterSet;
import com.elvaco.mvp.core.filter.LocationConfidenceFilter;
import com.elvaco.mvp.core.filter.RequestParametersConverter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

    return findAllMapMarkers(
      RequestParametersConverter.toFilterSet(
        parameters
      )
    );
  }

  private Set<MapMarker> findAllMapMarkers(FilterSet filters) {
    JPQLQuery<MapMarker> query = createQuery()
      .select(Projections.constructor(
        MapMarker.class,
        GATEWAY.id,
        GATEWAY_STATUS_LOG.status,
        ALARM_LOG.mask,
        LOCATION.latitude,
        LOCATION.longitude
      ));

    filters.add(new LocationConfidenceFilter(GeoCoordinate.HIGH_CONFIDENCE, ComparisonMode.EQUAL));

    new GatewayFilterQueryDslJpaVisitor().visitAndApply(filters, query);

    return new HashSet<>(query
      .distinct()
      .fetch());
  }
}
