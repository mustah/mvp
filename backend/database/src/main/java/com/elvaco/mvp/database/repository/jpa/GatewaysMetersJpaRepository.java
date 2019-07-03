package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.database.entity.gateway.GatewayMeterEntity;

public interface GatewaysMetersJpaRepository {

  void deleteAll();

  List<GatewayMeterEntity> findByLogicalMeterIdAndOrganisationId(
    UUID logicalMeterId,
    UUID organisationId
  );

  void saveOrUpdate(
    UUID organisationId,
    UUID gatewayId,
    UUID logicalMeterId,
    ZonedDateTime lastSeen
  );
}
