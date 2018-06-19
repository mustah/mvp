package com.elvaco.mvp.database.repository.jpa;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.queryfilters.LogicalMeterQueryFilters;
import com.elvaco.mvp.database.util.JoinIfNeededUtil;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.meter.QLocationEntity.locationEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity.physicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

@Repository
class LogicalMeterMapQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements MapMarkerJpaRepository {

  private static final QLocationEntity LOCATION =
    locationEntity;

  private static final QLogicalMeterEntity LOGICAL_METER =
    logicalMeterEntity;

  private static final QPhysicalMeterStatusLogEntity METER_STATUS_LOG =
    physicalMeterStatusLogEntity;

  private static final QPhysicalMeterEntity PHYSICAL_METER =
    physicalMeterEntity;

  @Autowired
  LogicalMeterMapQueryDslJpaRepository(EntityManager entityManager) {
    super(
      new JpaMetamodelEntityInformation<>(LogicalMeterEntity.class, entityManager.getMetamodel()),
      entityManager
    );
  }

  @Override
  public Set<MapMarker> findAllMapMarkers(RequestParameters parameters) {
    JPQLQuery<MapMarker> query = createQuery(toPredicate(parameters))
      .select(Projections.constructor(
        MapMarker.class,
        LOCATION.logicalMeterId,
        METER_STATUS_LOG.status,
        LOCATION.latitude,
        LOCATION.longitude
      ))
      .join(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.statusLogs, METER_STATUS_LOG)
      .join(LOGICAL_METER.location, LOCATION)
      .on(LOCATION.confidence.goe(GeoCoordinate.HIGH_CONFIDENCE))
      .distinct();

    JoinIfNeededUtil.joinGatewayFromLogicalMeter(query, parameters);

    return new HashSet<>(query.fetch());
  }

  private static Predicate toPredicate(RequestParameters parameters) {
    return new LogicalMeterQueryFilters().toExpression(parameters);
  }
}
