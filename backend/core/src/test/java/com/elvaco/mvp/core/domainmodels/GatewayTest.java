package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GatewayTest {

  @Test
  public void unknownGatewayStatusBeginsAtEpoch() {
    var organisationId = UUID.randomUUID();
    Gateway gateway = Gateway.builder()
      .organisationId(organisationId)
      .serial("serial")
      .productModel("productModel")
      .build();

    assertThat(gateway.currentStatus()).isEqualTo(StatusLogEntry.builder()
      .primaryKey(gateway.primaryKey())
      .status(StatusType.UNKNOWN)
      .start(ZonedDateTime.parse("1970-01-01T00:00:00Z[UTC]"))
      .build());
  }
}
