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
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.repository.querydsl.LogicalMeterFilterQueryDslVisitor;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.domainmodels.GeoCoordinate.CONFIDENCE_THRESHOLD;
import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;

@Repository
class LogicalMeterMapQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements MapMarkerJpaRepository {

  @Autowired
  LogicalMeterMapQueryDslJpaRepository(EntityManager entityManager) {
    super(entityManager, LogicalMeterEntity.class);
  }

  @Override
  public Set<MapMarker> findAllMapMarkers(RequestParameters parameters) {
    JPQLQuery<MapMarker> query = createQuery()
      .select(Projections.constructor(
        MapMarker.class,
        LOCATION.logicalMeterId,
        METER_STATUS_LOG.status,
        ALARM_LOG.mask,
        LOCATION.latitude,
        LOCATION.longitude
      ));

    Filters filters = toFilters(parameters);

    filters.add(new LocationConfidenceFilter(CONFIDENCE_THRESHOLD, ComparisonMode.EQUAL));

    new LogicalMeterFilterQueryDslVisitor().visitAndApply(filters, query);

    return new HashSet<>(query.distinct().fetch());
  }
}
