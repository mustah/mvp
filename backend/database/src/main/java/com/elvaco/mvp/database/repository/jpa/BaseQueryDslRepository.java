package com.elvaco.mvp.database.repository.jpa;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.entity.user.QOrganisationEntity;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.PathBuilder;
import org.jooq.Param;
import org.jooq.Query;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.QuerydslJpaRepository;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.NoRepositoryBean;

import static com.elvaco.mvp.database.entity.gateway.QGatewayEntity.gatewayEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity.physicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;
import static com.elvaco.mvp.database.entity.user.QOrganisationEntity.organisationEntity;

@NoRepositoryBean
abstract class BaseQueryDslRepository<T, I extends Serializable>
  extends QuerydslJpaRepository<T, I> {

  static final QGatewayEntity GATEWAY = gatewayEntity;
  static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;
  static final QPhysicalMeterEntity PHYSICAL_METER = physicalMeterEntity;
  static final QOrganisationEntity ORGANISATION = organisationEntity;
  static final QPhysicalMeterStatusLogEntity METER_STATUS_LOG = physicalMeterStatusLogEntity;

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

  <E> List<E> nativeQuery(Query query, Class<E> type) {
    var result = entityManager.createNativeQuery(query.getSQL(), type);

    int i = 0;
    for (Param<?> param : query.getParams().values()) {
      result.setParameter(i + 1, convertToDatabaseType(param));
      i++;
    }

    return result.getResultList();
  }

  private static <T> Object convertToDatabaseType(Param<T> param) {
    return param.getBinding().converter().to(param.getValue());
  }
}
