package com.elvaco.mvp.testing.repository;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.spi.repository.GatewaysMeters;

public class MockGatewaysMeters implements GatewaysMeters {

  @Override
  public void saveOrUpdate(
    UUID gatewayId,
    UUID logicalMeterId,
    UUID organisationId,
    ZonedDateTime lastSeen
  ) {}
}
