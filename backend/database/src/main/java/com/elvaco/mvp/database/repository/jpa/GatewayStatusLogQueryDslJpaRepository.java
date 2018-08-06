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
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity.gatewayStatusLogEntity;
import static com.querydsl.core.group.GroupBy.groupBy;

@Repository
class GatewayStatusLogQueryDslJpaRepository
  extends BaseQueryDslRepository<GatewayStatusLogEntity, Long>
  implements GatewayStatusLogJpaRepository {

  private static final QGatewayStatusLogEntity STATUS_LOG = gatewayStatusLogEntity;

  @Autowired
  GatewayStatusLogQueryDslJpaRepository(EntityManager entityManager) {
    super(entityManager, GatewayStatusLogEntity.class);
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
