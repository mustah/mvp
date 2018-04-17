package com.elvaco.mvp.core.spi.repository;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
public class GatewaysTest extends IntegrationTest {

  @Autowired
  Gateways gateways;

  @Autowired
  GatewayJpaRepository gatewayJpaRepository;

  @Autowired
  GatewayStatusLogJpaRepository gatewayStatusLogJpaRepository;

  @After
  public void tearDown() {
    gatewayStatusLogJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
  }

  @Test
  public void savingGatewaySavesLogs() {
    UUID gatewayId = randomUUID();
    ZonedDateTime start = ZonedDateTime.now();
    gateways.save(
      new Gateway(gatewayId, context().getOrganisationId(), "", "", Collections.emptyList(),
        Collections.singletonList(new StatusLogEntry<>(gatewayId, StatusType.ERROR, start))
      )
    );

    Gateway found = gateways.findById(gatewayId).get();
    assertThat(found.statusLogs).hasSize(1);
    assertThat(found.statusLogs.get(0).start).isEqualTo(start);
    assertThat(found.statusLogs.get(0).status).isEqualTo(StatusType.ERROR);
  }

}
