package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;

public class GatewayStatusLogJpaRepositoryImpl
  extends BaseQueryDslRepository<GatewayStatusLogEntity, Long>
  implements GatewayStatusLogJpaRepositoryCustom {

  @Autowired
  public GatewayStatusLogJpaRepositoryImpl(EntityManager entityManager) {
    super(
      new JpaMetamodelEntityInformation<>(
        GatewayStatusLogEntity.class,
        entityManager.getMetamodel()
      ),
      entityManager
    );
  }

  @Override
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
