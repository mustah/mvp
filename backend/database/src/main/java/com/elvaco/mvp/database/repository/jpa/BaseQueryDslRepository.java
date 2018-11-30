package com.elvaco.mvp.database.repository.jpa;

import java.io.Serializable;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.QMissingMeasurementEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.entity.user.QOrganisationEntity;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.QuerydslJpaRepository;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.NoRepositoryBean;

import static com.elvaco.mvp.database.entity.gateway.QGatewayEntity.gatewayEntity;
import static com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity.gatewayStatusLogEntity;
import static com.elvaco.mvp.database.entity.measurement.QMeasurementEntity.measurementEntity;
import static com.elvaco.mvp.database.entity.measurement.QMissingMeasurementEntity.missingMeasurementEntity;
import static com.elvaco.mvp.database.entity.meter.QLocationEntity.locationEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity.meterAlarmLogEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity.physicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;
import static com.elvaco.mvp.database.entity.user.QOrganisationEntity.organisationEntity;

@NoRepositoryBean
abstract class BaseQueryDslRepository<T, I extends Serializable>
  extends QuerydslJpaRepository<T, I> {

  static final QGatewayEntity GATEWAY = gatewayEntity;
  static final QLocationEntity LOCATION = locationEntity;
  static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;
  static final QGatewayStatusLogEntity GATEWAY_STATUS_LOG = gatewayStatusLogEntity;
  static final QPhysicalMeterEntity PHYSICAL_METER = physicalMeterEntity;
  static final QOrganisationEntity ORGANISATION = organisationEntity;
  static final QPhysicalMeterStatusLogEntity METER_STATUS_LOG = physicalMeterStatusLogEntity;
  static final QMeterAlarmLogEntity ALARM_LOG = meterAlarmLogEntity;
  static final QMeasurementEntity MEASUREMENT = measurementEntity;
  static final QMissingMeasurementEntity MISSING_MEASUREMENT = missingMeasurementEntity;

  protected final EntityManager entityManager;
  protected final EntityPath<T> path;
  protected final Querydsl querydsl;

  protected BaseQueryDslRepository(EntityManager entityManager, Class<T> entityClass) {
    super(
      new JpaMetamodelEntityInformation<>(entityClass, entityManager.getMetamodel()),
      entityManager
    );
    this.entityManager = entityManager;
    this.path = SimpleEntityPathResolver.INSTANCE.createPath(entityClass);
    this.querydsl = new Querydsl(
      entityManager,
      new PathBuilder<>(path.getType(), path.getMetadata())
    );
  }
}
