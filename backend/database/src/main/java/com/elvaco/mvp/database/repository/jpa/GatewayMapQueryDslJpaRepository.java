package com.elvaco.mvp.database.repository.jpa;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.repository.queryfilters.GatewayQueryFilters;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.gateway.QGatewayEntity.gatewayEntity;
import static com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity.gatewayStatusLogEntity;
import static com.elvaco.mvp.database.entity.meter.QLocationEntity.locationEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinLogicalMetersPhysicalMeter;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinMeterAlarmLogs;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinMeterStatusLogs;

@Repository
class GatewayMapQueryDslJpaRepository
  extends BaseQueryDslRepository<GatewayEntity, UUID>
  implements MapMarkerJpaRepository {

  private static final QLocationEntity LOCATION = locationEntity;
  private static final QGatewayEntity GATEWAY = gatewayEntity;
  private static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;
  private static final QGatewayStatusLogEntity GATEWAY_STATUS_LOG = gatewayStatusLogEntity;

  @Autowired
  GatewayMapQueryDslJpaRepository(EntityManager entityManager) {
    super(entityManager, GatewayEntity.class);
  }

  @Override
  public Set<MapMarker> findAllMapMarkers(RequestParameters parameters) {
    Predicate predicate = new GatewayQueryFilters().toExpression(parameters);
    JPQLQuery<MapMarker> query = createQuery(predicate)
      .select(Projections.constructor(
        MapMarker.class,
        GATEWAY.id,
        GATEWAY_STATUS_LOG.status,
        LOCATION.latitude,
        LOCATION.longitude
      ))
      .join(GATEWAY.meters, LOGICAL_METER)
      .leftJoin(GATEWAY.statusLogs, GATEWAY_STATUS_LOG)
      .join(LOGICAL_METER.location, LOCATION)
      .on(LOCATION.confidence.goe(GeoCoordinate.HIGH_CONFIDENCE));

    joinLogicalMetersPhysicalMeter(query, parameters);
    joinMeterStatusLogs(query, parameters);
    joinMeterAlarmLogs(query, parameters);

    return new HashSet<>(query.distinct().fetch());
  }
}
