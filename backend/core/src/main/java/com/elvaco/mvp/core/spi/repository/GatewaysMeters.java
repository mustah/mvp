package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.util.UUID;

public interface GatewaysMeters {

  void saveOrUpdate(
    UUID gatewayId,
    UUID logicalMeterId,
    UUID organisationId,
    ZonedDateTime lastSeen
  );
}
