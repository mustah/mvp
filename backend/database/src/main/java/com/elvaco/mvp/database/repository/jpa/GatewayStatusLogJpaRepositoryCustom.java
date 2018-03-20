package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface GatewayStatusLogJpaRepositoryCustom
  extends QueryDslPredicateExecutor<GatewayStatusLogEntity> {

  Map<UUID, List<GatewayStatusLogEntity>> findAllGroupedByGatewayId(@Nullable Predicate predicate);
}
