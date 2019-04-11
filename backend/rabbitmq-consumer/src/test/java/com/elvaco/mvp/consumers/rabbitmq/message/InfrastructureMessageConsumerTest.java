package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.consumers.rabbitmq.dto.InfrastructureStatusMessageDto;
import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Organisation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class InfrastructureMessageConsumerTest extends MessageConsumerTest {

  public static final JsonNode JSON = toJsonNode("{\"testkey\":\"testvalue\"}");
  public static final JsonNode JSON_OTHER = toJsonNode("{\"otherKey\":\"otherValue\"}");
  private InfrastructureMessageConsumer consumer;

  @Before
  public void setUp() {
    super.setUp();

    consumer = new InfrastructureStatusMessageConsumer(
      gatewayUseCases
    );
  }

  @Test
  public void acceptNewExtraInfo() {
    String serial = randomUUID().toString();

    Organisation organisation = saveDefaultOrganisation();
    gateways.save(Gateway.builder()
      .organisationId(organisation.id)
      .serial(serial)
      .extraInfo(JSON_OTHER)
      .productModel("6110")
      .build());

    var dto = new InfrastructureStatusMessageDto(
      serial,
      JSON
    );

    consumer.accept(dto);

    assertThat(gateways.findAll())
      .hasSize(1)
      .extracting(g -> g.serial, g -> g.extraInfo)
      .containsExactly(tuple(serial, JSON));
  }

  @Test
  public void ignoreExtraInfoForUnknownGateway() {
    String serial = randomUUID().toString();
    var dto = new InfrastructureStatusMessageDto(
      serial,
      JSON
    );

    consumer.accept(dto);

    assertThat(gateways.findAll()).isEmpty();
  }

  @Test
  public void ignoreExtraInfoForAmbiguousGateway() {
    String serial = randomUUID().toString();

    var organisation1 = saveDefaultOrganisation();
    var organisation2 = saveOtherOrganisation();
    var gateway1 = gateways.save(Gateway.builder()
      .organisationId(organisation1.id)
      .serial(serial)
      .extraInfo(JSON)
      .productModel("6110")
      .build());
    var gateway2 = gateways.save(Gateway.builder()
      .organisationId(organisation2.id)
      .serial(serial)
      .extraInfo(JSON_OTHER)
      .productModel("6110")
      .build());

    var newJson = ((ObjectNode) JSON.deepCopy()).put("asdf", "asdf");

    var dto = new InfrastructureStatusMessageDto(
      serial,
      newJson
    );

    consumer.accept(dto);

    assertThat(gateways.findAll()).containsExactlyInAnyOrder(gateway1, gateway2);
  }

  private Organisation saveDefaultOrganisation() {
    return organisations.save(new Organisation(
      randomUUID(),
      ORGANISATION_EXTERNAL_ID,
      ORGANISATION_SLUG,
      ORGANISATION_EXTERNAL_ID
    ));
  }

  private Organisation saveOtherOrganisation() {
    var uuid = randomUUID();
    return organisations.save(new Organisation(
      uuid,
      uuid.toString(),
      uuid.toString(),
      uuid.toString()
    ));
  }
}
