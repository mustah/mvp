package com.elvaco.mvp.consumers.rabbitmq;

import com.elvaco.mvp.consumers.rabbitmq.dto.InfrastructureStatusMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.message.InfrastructureMessageConsumer;
import com.elvaco.mvp.consumers.rabbitmq.message.InfrastructureStatusMessageConsumer;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.testdata.IntegrationTest;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class InfrastructureStatusMessageConsumerTest extends IntegrationTest {

  private static final JsonNode JSON = toJsonNode("{\"testkey\":\"testvalue\"}");
  private static final JsonNode JSON2 = toJsonNode("{\"testkey\":\"testvalue2\"}");

  @Autowired
  private GatewayUseCases gatewayUseCases;

  private InfrastructureMessageConsumer consumer;

  @Before
  public void setUp() {
    authenticate(context().superAdmin);
    consumer = new InfrastructureStatusMessageConsumer(gatewayUseCases);
  }

  @Transactional
  @Test
  public void acceptNewExtraInfo() {
    Gateway gateway = given(gateway());
    var dto = new InfrastructureStatusMessageDto(gateway.serial, JSON);
    consumer.accept(dto);

    assertThat(gatewayJpaRepository.findBySerial(dto.eui))
      .extracting(g -> g.serial, g -> g.extraInfo.getJson())
      .containsExactly(tuple(dto.eui, dto.properties));
  }

  @Transactional
  @Test
  public void acceptNewExtraInfoViaLogicalMeter() {
    Gateway gateway = given(gateway());
    LogicalMeter logicalMeter = given(logicalMeter().gateway(gateway));

    var dto = new InfrastructureStatusMessageDto(gateway.serial, JSON);
    consumer.accept(dto);
    //This is saving result to cache (logicalMeter.organisationIdExternalId)
    logicalMeter =  logicalMeters.findByOrganisationIdAndExternalId(logicalMeter.organisationId,
      logicalMeter.externalId).get();
    assertThat(logicalMeter.gateways)
      .extracting(g -> tuple(g.serial, g.extraInfo))
      .containsExactly(tuple(dto.eui, dto.properties));

    dto = new InfrastructureStatusMessageDto(gateway.serial, JSON2);
    consumer.accept(dto);

    //here we pray for the cache being evicted so we get the latest extraInfo
    logicalMeter =  logicalMeters.findByOrganisationIdAndExternalId(logicalMeter.organisationId,
      logicalMeter.externalId).get();
    assertThat(logicalMeter.gateways)
      .extracting(g -> g.serial, g -> g.extraInfo)
      .containsExactly(tuple(dto.eui, dto.properties));
  }

  @Transactional
  @Test
  public void ignoreMissingGateway() {
    var dto = new InfrastructureStatusMessageDto(randomUUID().toString(), JSON);

    consumer.accept(dto);

    assertThat(gatewayJpaRepository.findBySerial(dto.eui)).isEmpty();
  }

  @Transactional
  @Test
  public void ignoreAmbiguousGateway() {
    var sameSerial = "asdf";
    var organisation1 = given(organisation());
    var organisation2 = given(organisation());
    given(
      gateway().serial(sameSerial).organisationId(organisation1.id),
      gateway().serial(sameSerial).organisationId(organisation2.id)
    );
    var dto = new InfrastructureStatusMessageDto(sameSerial, JSON);

    assertThat(gatewayJpaRepository.findBySerial(dto.eui))
      .extracting(g -> g.extraInfo.asJsonString())
      .containsExactly("{}", "{}");

    consumer.accept(dto);

    assertThat(gatewayJpaRepository.findBySerial(dto.eui))
      .extracting(g -> g.extraInfo.asJsonString())
      .containsExactly("{}", "{}");
  }
}
