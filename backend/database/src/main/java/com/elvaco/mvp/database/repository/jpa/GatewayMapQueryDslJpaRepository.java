package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.gateway.QGatewayEntity.gatewayEntity;
import static com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity.gatewayStatusLogEntity;
import static com.elvaco.mvp.database.entity.meter.QLocationEntity.locationEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;

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
    super(
      new JpaMetamodelEntityInformation<>(GatewayEntity.class, entityManager.getMetamodel()),
      entityManager
    );
  }

  @Override
  public List<MapMarker> findAllMapMarkers(RequestParameters parameters) {
    return createQuery(toPredicate(parameters))
      .select(Projections.constructor(
        MapMarker.class,
        GATEWAY.id,
        GATEWAY_STATUS_LOG.status,
        LOCATION.latitude,
        LOCATION.longitude
      ))
      .innerJoin(GATEWAY.meters, LOGICAL_METER)
      .leftJoin(GATEWAY.statusLogs, GATEWAY_STATUS_LOG)
      .innerJoin(LOGICAL_METER.location, LOCATION)
      .on(LOCATION.confidence.goe(GeoCoordinate.HIGH_CONFIDENCE))
      .fetch();
  }

  private static Predicate toPredicate(RequestParameters parameters) {
    return new GatewayQueryFilters().toExpression(parameters);
  }
}
