package com.elvaco.mvp.database.repository.jpa;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.filter.RequestParametersMapper;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
    return findAllMapMarkers(RequestParametersMapper.toFilters(parameters));
  }

  private Set<MapMarker> findAllMapMarkers(Filters filters) {
    LogicalMeterFilterQueryDslVisitor visitor = new LogicalMeterFilterQueryDslVisitor();
    JPQLQuery<MapMarker> query = createQuery()
      .select(Projections.constructor(
        MapMarker.class,
        LOCATION.logicalMeterId,
        METER_STATUS_LOG.status,
        ALARM_LOG.mask,
        LOCATION.latitude,
        LOCATION.longitude
      ));
    visitor.visitAndApply(filters, query);

    return new HashSet<>(query.distinct().fetch());
  }
}
