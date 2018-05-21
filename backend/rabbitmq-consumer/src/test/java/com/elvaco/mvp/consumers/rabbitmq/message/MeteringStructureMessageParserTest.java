package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringStructureMessageDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MessageType;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.consumers.rabbitmq.message.JsonFileReader.parseJsonFile;
import static org.assertj.core.api.Assertions.assertThat;

public class MeteringStructureMessageParserTest {

  private static final String CRON_FIFTEEN_MINUTES = "*/15 * * * *";

  private MeteringMessageParser messageParser;

  @Before
  public void setUp() {
    messageParser = new MeteringMessageParser();
  }

  @Test
  public void heatMeter() {
    String jsonMessage = parseJsonFile("messages/reference-info-valid-heat-meter.json");

    MeteringStructureMessageDto parsedMessage =
      messageParser.parseStructureMessage(jsonMessage).orElse(null);

    assertThat(parsedMessage).isNotNull();
    assertThat(parsedMessage.messageType).isEqualTo(MessageType.METERING_METER_STRUCTURE_V_1_0);
    assertThat(parsedMessage.facility.id).isEqualTo("ABC-123");
    assertThat(parsedMessage.facility.country).isEqualTo("Sweden");
    assertThat(parsedMessage.facility.city).isEqualTo("Perstorp");
    assertThat(parsedMessage.facility.address).isEqualTo("Duvstigen 8C");
    assertThat(parsedMessage.gateway).isNotNull();
    assertThat(parsedMessage.gateway.id).isEqualTo("12031925");
    assertThat(parsedMessage.gateway.productModel).isEqualTo("CMi2110");
    assertThat(parsedMessage.meter.id).isEqualTo("1");
    assertThat(parsedMessage.meter.medium).isEqualTo("Heat, Return temp");
    assertThat(parsedMessage.meter.manufacturer).isEqualTo("ELV");
    assertThat(parsedMessage.meter.cron).isEqualTo(CRON_FIFTEEN_MINUTES);
    assertThat(parsedMessage.organisationId).isEqualTo("Organisation, Incorporated");
    assertThat(parsedMessage.sourceSystemId).isEqualTo("The Source System");
  }

  @Test
  public void gasMeter() {
    String jsonMessage = parseJsonFile("messages/reference-info-valid-gas-meter.json");

    MeteringStructureMessageDto parsedMessage =
      messageParser.parseStructureMessage(jsonMessage).orElse(null);

    assertThat(parsedMessage).isNotNull();
    assertThat(parsedMessage.messageType).isEqualTo(MessageType.METERING_METER_STRUCTURE_V_1_0);
    assertThat(parsedMessage.facility.id).isEqualTo("ABC-121");
    assertThat(parsedMessage.facility.country).isEqualTo("Sweden");
    assertThat(parsedMessage.facility.city).isEqualTo("Älmhult");
    assertThat(parsedMessage.facility.address).isEqualTo("I lived here");
    assertThat(parsedMessage.gateway).isNotNull();
    assertThat(parsedMessage.gateway.id).isEqualTo("12031928");
    assertThat(parsedMessage.gateway.productModel).isEqualTo("CMi2110");
    assertThat(parsedMessage.meter.id).isEqualTo("3");
    assertThat(parsedMessage.meter.medium).isEqualTo("Gas");
    assertThat(parsedMessage.meter.manufacturer).isEqualTo("ELV");
    assertThat(parsedMessage.meter.cron).isEqualTo(CRON_FIFTEEN_MINUTES);
    assertThat(parsedMessage.organisationId).isEqualTo("Bromölla bikers");
    assertThat(parsedMessage.sourceSystemId).isEqualTo("The flipside");
  }

  @Test
  public void parseMalformedStructureMessage() {
    assertThat(messageParser.parseStructureMessage("")).isEmpty();
    assertThat(messageParser.parseStructureMessage("{\"foo\": 1999}")).isEmpty();
    assertThat(messageParser.parseStructureMessage("}}}}}}}}}}}}[]]}}}}}}}}}}¡")).isEmpty();
  }

  @Test
  public void missingGateway() {
    String missingGateway = parseJsonFile("messages/reference-info-missing-gateway.json");
    assertThat(messageParser.parseStructureMessage(missingGateway)).isPresent();
  }

  @Test
  public void missingMeter() {
    String missingMeter = parseJsonFile("messages/reference-info-missing-meter.json");
    assertThat(messageParser.parseStructureMessage(missingMeter)).isPresent();
  }

  @Test
  public void emptyGateway() {
    String emptyGateway = parseJsonFile("messages/reference-info-empty-gateway.json");
    assertThat(messageParser.parseStructureMessage(emptyGateway)).isPresent();
  }

  @Test
  public void emptyMeter() {
    String emptyMeter = parseJsonFile("messages/reference-info-empty-meter.json");
    assertThat(messageParser.parseStructureMessage(emptyMeter)).isPresent();
  }

  @Test
  public void readIntervalCanBeNull() {
    String message = parseJsonFile("messages/metering-structure-message-no-cron.json");

    MeteringStructureMessageDto parsedMessage = messageParser.parseStructureMessage(message).get();

    assertThat(parsedMessage.meter.cron).isNull();
  }
}
