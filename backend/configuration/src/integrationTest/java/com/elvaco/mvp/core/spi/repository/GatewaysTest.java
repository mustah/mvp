package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Pk;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@Transactional
public class GatewaysTest extends IntegrationTest {

  @Autowired
  private Gateways gateways;

  @After
  public void tearDown() {
    gatewayStatusLogJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
  }

  @Test
  public void savingGatewaySavesLogs() {
    var gatewayId = randomUUID();
    var start = ZonedDateTime.now();
    var primaryKey = new Pk(gatewayId, context().organisationId());
    gateways.save(Gateway.builder()
      .id(gatewayId)
      .organisationId(context().organisationId())
      .serial("")
      .productModel("")
      .statusLog(StatusLogEntry.builder()
        .primaryKey(primaryKey)
        .status(StatusType.ERROR)
        .start(start)
        .build())
      .build()
    );

    Gateway found = gateways.findById(gatewayId).get();
    assertThat(found.statusLogs).hasSize(1);
    assertThat(found.statusLogs).extracting("start").containsOnly(start);
    assertThat(found.statusLogs).extracting("status").containsOnly(StatusType.ERROR);
  }

  @Test
  public void findGatewayBySerialIgnoreCase() {
    var gatewayId = randomUUID();
    var gatewaySerial = "F612A3";
    gateways.save(Gateway.builder()
      .id(gatewayId)
      .organisationId(context().organisationId())
      .serial(gatewaySerial)
      .productModel("")
      .build());

    assertThat(gateways.findById(gatewayId))
      .isPresent()
      .get().extracting(g -> g.id, g -> g.serial)
      .contains(gatewayId, gatewaySerial);

    assertThat(gateways.findBy(gatewaySerial.toLowerCase()))
      .hasSize(1)
      .extracting(g -> g.id, g -> g.serial)
      .containsExactly(tuple(gatewayId, gatewaySerial));
  }
}
