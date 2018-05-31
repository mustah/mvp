package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
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
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.meter.QLocationEntity.locationEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity.physicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

@Repository
class MeterMapQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements MapMarkerJpaRepository {

  private static final QLocationEntity LOCATION = locationEntity;

  private static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;

  private static final QPhysicalMeterStatusLogEntity STATUS_LOG = physicalMeterStatusLogEntity;

  private static final QPhysicalMeterEntity PHYSICAL_METER = physicalMeterEntity;

  @Autowired
  MeterMapQueryDslJpaRepository(EntityManager entityManager) {
    super(
      new JpaMetamodelEntityInformation<>(LogicalMeterEntity.class, entityManager.getMetamodel()),
      entityManager
    );
  }

  @Override
  public List<MapMarker> findAllMapMarkers(RequestParameters parameters) {
    return createQuery(toPredicate(parameters))
      .select(Projections.constructor(
        MapMarker.class,
        LOCATION.logicalMeterId,
        STATUS_LOG.status,
        LOCATION.latitude,
        LOCATION.longitude
      ))
      .innerJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG)
      .innerJoin(LOGICAL_METER.location, LOCATION)
      .on(LOCATION.confidence.goe(GeoCoordinate.HIGH_CONFIDENCE))
      .fetch();
  }

  private static Predicate toPredicate(RequestParameters parameters) {
    return new LogicalMeterQueryFilters().toExpression(parameters);
  }
}
