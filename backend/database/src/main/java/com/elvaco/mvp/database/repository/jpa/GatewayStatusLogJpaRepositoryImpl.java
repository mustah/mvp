package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

public class GatewayStatusLogJpaRepositoryImpl
  extends QueryDslJpaRepository<GatewayStatusLogEntity, Long>
  implements GatewayStatusLogJpaRepositoryCustom {

  private final EntityManager entityManager;
  private final EntityPath<GatewayStatusLogEntity> path;
  private final Querydsl querydsl;

  @Autowired
  GatewayStatusLogJpaRepositoryImpl(EntityManager entityManager) {
    this(entityManager, SimpleEntityPathResolver.INSTANCE);
  }

  private GatewayStatusLogJpaRepositoryImpl(
    EntityManager entityManager,
    EntityPathResolver resolver
  ) {
    this(
      new JpaMetamodelEntityInformation<>(
        GatewayStatusLogEntity.class,
        entityManager.getMetamodel()
      ),
      entityManager,
      resolver
    );
  }

  private GatewayStatusLogJpaRepositoryImpl(
    JpaEntityInformation<GatewayStatusLogEntity, Long> entityInformation,
    EntityManager entityManager,
    EntityPathResolver resolver
  ) {
    super(entityInformation, entityManager);
    this.entityManager = entityManager;
    this.path = resolver.createPath(entityInformation.getJavaType());
    PathBuilder<GatewayStatusLogEntity> builder = new PathBuilder<>(
      path.getType(),
      path.getMetadata()
    );
    this.querydsl = new Querydsl(entityManager, builder);
  }

  public Map<UUID, List<GatewayStatusLogEntity>> findAllGroupedByGatewayId(
    @Nullable Predicate predicate
  ) {
    JPQLQuery<Void> query = new JPAQuery<>(entityManager);
    QGatewayStatusLogEntity gatewayStatusLogEntity = QGatewayStatusLogEntity.gatewayStatusLogEntity;
    return query.from(gatewayStatusLogEntity)
      .where(predicate)
      .transform(
        GroupBy.groupBy(gatewayStatusLogEntity.gatewayId).as(GroupBy.list(gatewayStatusLogEntity))
      );
  }
}
