package com.elvaco.mvp.database.repository.mappers;

import java.time.ZonedDateTime;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.gateway.GatewayPk;
import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.JsonField;

import org.junit.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class GatewayEntityMapperTest {

  @Test
  public void toDomainModel_StatusLogs_OneEntry() {
    var gatewayEntity = new GatewayEntity();
    gatewayEntity.pk = new EntityPk(randomUUID(), randomUUID());

    GatewayStatusLogEntity statusLogEntity = new GatewayStatusLogEntity();
    statusLogEntity.gatewayId = new GatewayPk(gatewayEntity.pk.id, gatewayEntity.pk.organisationId);
    statusLogEntity.status = StatusType.OK;
    ZonedDateTime start = ZonedDateTime.parse("2001-01-01T10:14:00.00Z");
    statusLogEntity.start = start;
    statusLogEntity.stop = ZonedDateTime.parse("2001-01-02T10:14:00.00Z");

    gatewayEntity.statusLogs = Set.of(statusLogEntity);
    gatewayEntity.extraInfo = new JsonField();

    var gateway = GatewayEntityMapper.toDomainModel(gatewayEntity);

    assertThat(gateway.statusLogs)
      .isNotNull()
      .hasSize(1)
      .extracting(s -> s.status, s -> s.start)
      .containsExactly(tuple(StatusType.OK, start));
  }

  @Test
  public void toDomainModel_StatusLogs_None() {
    var gatewayEntity = new GatewayEntity();
    gatewayEntity.pk = new EntityPk(randomUUID(), randomUUID());
    gatewayEntity.extraInfo = new JsonField();

    var gateway = GatewayEntityMapper.toDomainModel(gatewayEntity);

    assertThat(gateway.statusLogs)
      .isNotNull()
      .hasSize(0);
  }
}
