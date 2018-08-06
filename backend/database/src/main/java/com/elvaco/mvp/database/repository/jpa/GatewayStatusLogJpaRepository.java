package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.querydsl.core.types.Predicate;

public interface GatewayStatusLogJpaRepository {

  <S extends GatewayStatusLogEntity> S save(S entity);

  <S extends GatewayStatusLogEntity> List<S> save(Iterable<S> entities);

  Map<UUID, List<GatewayStatusLogEntity>> findAllGroupedByGatewayId(@Nullable Predicate predicate);

  void deleteAll();
}
