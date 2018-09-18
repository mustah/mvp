package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.querydsl.core.group.GroupBy.groupBy;

@Repository
class GatewayStatusLogQueryDslJpaRepository
  extends BaseQueryDslRepository<GatewayStatusLogEntity, Long>
  implements GatewayStatusLogJpaRepository {

  @Autowired
  GatewayStatusLogQueryDslJpaRepository(EntityManager entityManager) {
    super(entityManager, GatewayStatusLogEntity.class);
  }

  @Override
  public Map<UUID, List<GatewayStatusLogEntity>> findAllGroupedByGatewayId(
    @Nullable Predicate predicate
  ) {
    return createQuery(predicate)
      .orderBy(GATEWAY_STATUS_LOG.start.desc(), GATEWAY_STATUS_LOG.stop.desc())
      .transform(groupBy(GATEWAY_STATUS_LOG.gatewayId).as(GroupBy.list(GATEWAY_STATUS_LOG)));
  }
}
