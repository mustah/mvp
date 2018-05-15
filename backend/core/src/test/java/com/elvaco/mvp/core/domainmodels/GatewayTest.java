package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GatewayTest {
  @Test
  public void unknownGatewayStatusBeginsAtEpoch() {
    Gateway gateway = new Gateway(UUID.randomUUID(), UUID.randomUUID(), "serial", "productModel");
    assertThat(gateway.currentStatus()).isEqualTo(
      new StatusLogEntry<>(
        gateway.id,
        StatusType.UNKNOWN,
        ZonedDateTime.parse("1970-01-01T00:00:00Z[UTC]")
      )
    );
  }
}
