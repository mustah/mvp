package com.elvaco.mvp.database.repository.jpa;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.repository.queryfilters.LogicalMeterQueryFilters;
import com.elvaco.mvp.database.repository.queryfilters.MeterAlarmLogQueryFilters;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinLogicalMeterGateways;

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
    Predicate predicate = new LogicalMeterQueryFilters().toExpression(parameters);

    JPQLQuery<MapMarker> query = createQuery(predicate)
      .select(Projections.constructor(
        MapMarker.class,
        LOCATION.logicalMeterId,
        METER_STATUS_LOG.status,
        ALARM_LOG.mask,
        LOCATION.latitude,
        LOCATION.longitude
      ))
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.statusLogs, METER_STATUS_LOG)
      .join(LOGICAL_METER.location, LOCATION)
      .on(LOCATION.confidence.goe(GeoCoordinate.HIGH_CONFIDENCE))
      .leftJoin(PHYSICAL_METER.alarms, ALARM_LOG)
      .on(MeterAlarmLogQueryFilters.isWithinPeriod(parameters));

    joinLogicalMeterGateways(query, parameters);

    return new HashSet<>(query.distinct().fetch());
  }
}
