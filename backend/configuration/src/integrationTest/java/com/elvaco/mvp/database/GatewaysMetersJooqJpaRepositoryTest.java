package com.elvaco.mvp.database;

import java.time.ZonedDateTime;

import com.elvaco.mvp.database.repository.jpa.GatewaysMetersJooqJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class GatewaysMetersJooqJpaRepositoryTest extends IntegrationTest {

  @Test
  @Transactional
  public void saveOrUpdateLastSeenWhenNull() {
    var logicalMeter = given(logicalMeter().gateway(gateway().created(null)
      .lastSeen(null)
      .build()));

    ZonedDateTime now = ZonedDateTime.now();
    gatewaysMetersJpaRepository.saveOrUpdate(
      logicalMeter.organisationId,
      logicalMeter.gateways.stream().findFirst().orElseThrow().id,
      logicalMeter.id,
      now
    );

    TestTransaction.flagForCommit();
    TestTransaction.end();

    assertThat(((GatewaysMetersJooqJpaRepository) gatewaysMetersJpaRepository).findAll())
      .hasSize(1)
      .extracting(gm -> gm.lastSeen.toInstant(), gm -> gm.created)
      .containsExactly(tuple(now.toInstant(), null));
  }
}
