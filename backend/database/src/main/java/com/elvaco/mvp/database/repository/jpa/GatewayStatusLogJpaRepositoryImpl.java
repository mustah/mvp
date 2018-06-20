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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;

import static com.querydsl.core.group.GroupBy.groupBy;

public class GatewayStatusLogJpaRepositoryImpl
  extends BaseQueryDslRepository<GatewayStatusLogEntity, Long>
  implements GatewayStatusLogJpaRepositoryCustom {

  private static final QGatewayStatusLogEntity STATUS_LOG =
    QGatewayStatusLogEntity.gatewayStatusLogEntity;

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
    return createQuery(predicate)
      .orderBy(STATUS_LOG.start.desc(), STATUS_LOG.stop.desc())
      .transform(groupBy(STATUS_LOG.gatewayId).as(GroupBy.list(STATUS_LOG)));
  }
}
