package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface GatewayJpaRepository
  extends JpaRepository<GatewayEntity, Long>, QueryDslPredicateExecutor<GatewayEntity> {

  @Override
  List<GatewayEntity> findAll(Predicate predicate);

  List<GatewayEntity> findAllByOrganisationId(UUID organisationId);

  Optional<GatewayEntity> findByOrganisationIdAndProductModelAndSerial(
    UUID organisationId,
    String productModel,
    String serial
  );

  Optional<GatewayEntity> findById(UUID id);

  Optional<GatewayEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);
}
