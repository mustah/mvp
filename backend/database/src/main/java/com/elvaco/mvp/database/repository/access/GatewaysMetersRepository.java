package com.elvaco.mvp.database.repository.access;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.spi.repository.GatewaysMeters;
import com.elvaco.mvp.database.repository.jpa.GatewaysMetersJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GatewaysMetersRepository implements GatewaysMeters {

  private final GatewaysMetersJpaRepository gatewaysMetersJpaRepository;

  @Override
  public void saveOrUpdate(
    UUID gatewayId,
    UUID logicalMeterId,
    UUID organisationId,
    ZonedDateTime lastSeen
  ) {
    gatewaysMetersJpaRepository.saveOrUpdate(
      organisationId,
      gatewayId,
      logicalMeterId,
      lastSeen
    );
  }
}
