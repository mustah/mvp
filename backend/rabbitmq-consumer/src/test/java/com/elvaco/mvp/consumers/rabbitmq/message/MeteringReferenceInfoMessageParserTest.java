package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.producers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.consumers.rabbitmq.message.JsonFileReader.parseJsonFile;
import static org.assertj.core.api.Assertions.assertThat;

public class MeteringReferenceInfoMessageParserTest {

  private static final String CRON_FIFTEEN_MINUTES = "*/15 * * * *";

  private MeteringMessageParser messageParser;

  @Before
  public void setUp() {
    messageParser = new MeteringMessageParser();
  }

  @Test
  public void heatMeter() {
    String jsonMessage = parseJsonFile("messages/reference-info-valid-heat-meter.json");

    MeteringReferenceInfoMessageDto parsedMessage =
      messageParser.parseReferenceInfoMessage(jsonMessage).orElse(null);

    assertThat(parsedMessage).isNotNull();
    assertThat(parsedMessage.messageType).isEqualTo(MessageType.METERING_REFERENCE_INFO_V_1_0);
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

    MeteringReferenceInfoMessageDto parsedMessage =
      messageParser.parseReferenceInfoMessage(jsonMessage).orElse(null);

    assertThat(parsedMessage).isNotNull();
    assertThat(parsedMessage.messageType).isEqualTo(MessageType.METERING_REFERENCE_INFO_V_1_0);
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
  public void waterMeter() {
    String jsonMessage = parseJsonFile("messages/reference-info-valid-water-meter.json");

    MeteringReferenceInfoMessageDto parsedMessage =
      messageParser.parseReferenceInfoMessage(jsonMessage).orElse(null);

    assertThat(parsedMessage).isNotNull();
    assertThat(parsedMessage.messageType).isEqualTo(MessageType.METERING_REFERENCE_INFO_V_1_0);
    assertThat(parsedMessage.facility.id).isEqualTo("ABC-121");
    assertThat(parsedMessage.facility.country).isEqualTo("Sweden");
    assertThat(parsedMessage.facility.city).isEqualTo("Ankeborg");
    assertThat(parsedMessage.facility.address).isEqualTo("Kalles hus");
    assertThat(parsedMessage.gateway).isNotNull();
    assertThat(parsedMessage.gateway.id).isEqualTo("12031978");
    assertThat(parsedMessage.gateway.productModel).isEqualTo("CMi2110");
    assertThat(parsedMessage.meter.id).isEqualTo("64");
    assertThat(parsedMessage.meter.medium).isEqualTo("Water");
    assertThat(parsedMessage.meter.manufacturer).isEqualTo("ELV");
    assertThat(parsedMessage.meter.cron).isEqualTo(CRON_FIFTEEN_MINUTES);
    assertThat(parsedMessage.organisationId).isEqualTo("Anka AB");
    assertThat(parsedMessage.sourceSystemId).isEqualTo("AAB");
  }

  @Test
  public void hotWaterMeter() {
    String jsonMessage = parseJsonFile("messages/reference-info-valid-hot-water-meter.json");

    MeteringReferenceInfoMessageDto parsedMessage =
      messageParser.parseReferenceInfoMessage(jsonMessage).orElse(null);

    assertThat(parsedMessage).isNotNull();
    assertThat(parsedMessage.messageType).isEqualTo(MessageType.METERING_REFERENCE_INFO_V_1_0);
    assertThat(parsedMessage.facility.id).isEqualTo("ABC-121");
    assertThat(parsedMessage.facility.country).isEqualTo("Sweden");
    assertThat(parsedMessage.facility.city).isEqualTo("Ankeborg");
    assertThat(parsedMessage.facility.address).isEqualTo("Kajsas hus");
    assertThat(parsedMessage.gateway).isNotNull();
    assertThat(parsedMessage.gateway.id).isEqualTo("12031979");
    assertThat(parsedMessage.gateway.productModel).isEqualTo("CMi2110");
    assertThat(parsedMessage.meter.id).isEqualTo("666");
    assertThat(parsedMessage.meter.medium).isEqualTo("Hot water");
    assertThat(parsedMessage.meter.manufacturer).isEqualTo("ELV");
    assertThat(parsedMessage.meter.cron).isEqualTo(CRON_FIFTEEN_MINUTES);
    assertThat(parsedMessage.organisationId).isEqualTo("Anka AB");
    assertThat(parsedMessage.sourceSystemId).isEqualTo("AAB");
  }

  @Test
  public void coldWaterMeter() {
    String jsonMessage = parseJsonFile("messages/reference-info-valid-cold-water-meter.json");

    MeteringReferenceInfoMessageDto parsedMessage =
      messageParser.parseReferenceInfoMessage(jsonMessage).orElse(null);

    assertThat(parsedMessage).isNotNull();
    assertThat(parsedMessage.messageType).isEqualTo(MessageType.METERING_REFERENCE_INFO_V_1_0);
    assertThat(parsedMessage.facility.id).isEqualTo("ABC-121");
    assertThat(parsedMessage.facility.country).isEqualTo("Sweden");
    assertThat(parsedMessage.facility.city).isEqualTo("Ankeborg");
    assertThat(parsedMessage.facility.address).isEqualTo("Kajsas hus");
    assertThat(parsedMessage.gateway).isNotNull();
    assertThat(parsedMessage.gateway.id).isEqualTo("12031979");
    assertThat(parsedMessage.gateway.productModel).isEqualTo("CMi2110");
    assertThat(parsedMessage.meter.id).isEqualTo("65");
    assertThat(parsedMessage.meter.medium).isEqualTo("Cold water");
    assertThat(parsedMessage.meter.manufacturer).isEqualTo("ELV");
    assertThat(parsedMessage.meter.cron).isEqualTo(CRON_FIFTEEN_MINUTES);
    assertThat(parsedMessage.organisationId).isEqualTo("Anka AB");
    assertThat(parsedMessage.sourceSystemId).isEqualTo("AAB");
  }

  @Test
  public void electricityMeter() {
    String jsonMessage = parseJsonFile("messages/reference-info-valid-electricity-meter.json");

    MeteringReferenceInfoMessageDto parsedMessage =
      messageParser.parseReferenceInfoMessage(jsonMessage).orElse(null);

    assertThat(parsedMessage).isNotNull();
    assertThat(parsedMessage.messageType).isEqualTo(MessageType.METERING_REFERENCE_INFO_V_1_0);
    assertThat(parsedMessage.facility.id).isEqualTo("ABC-121");
    assertThat(parsedMessage.facility.country).isEqualTo("Sweden");
    assertThat(parsedMessage.facility.city).isEqualTo("Ankeborg");
    assertThat(parsedMessage.facility.address).isEqualTo("Kajsas hus");
    assertThat(parsedMessage.gateway).isNotNull();
    assertThat(parsedMessage.gateway.id).isEqualTo("12031979");
    assertThat(parsedMessage.gateway.productModel).isEqualTo("CMi2110");
    assertThat(parsedMessage.meter.id).isEqualTo("90");
    assertThat(parsedMessage.meter.medium).isEqualTo("Electricity");
    assertThat(parsedMessage.meter.manufacturer).isEqualTo("ELV");
    assertThat(parsedMessage.meter.cron).isEqualTo(CRON_FIFTEEN_MINUTES);
    assertThat(parsedMessage.organisationId).isEqualTo("Anka AB");
    assertThat(parsedMessage.sourceSystemId).isEqualTo("AAB");
  }

  @Test
  public void roomSensorMeter() {
    String jsonMessage = parseJsonFile("messages/reference-info-valid-room-sensor.json");

    MeteringReferenceInfoMessageDto parsedMessage =
      messageParser.parseReferenceInfoMessage(jsonMessage).orElse(null);

    assertThat(parsedMessage).isNotNull();
    assertThat(parsedMessage.messageType).isEqualTo(MessageType.METERING_REFERENCE_INFO_V_1_0);
    assertThat(parsedMessage.facility.id).isEqualTo("ABC-121");
    assertThat(parsedMessage.facility.country).isEqualTo("Sweden");
    assertThat(parsedMessage.facility.city).isEqualTo("Ankeborg");
    assertThat(parsedMessage.facility.address).isEqualTo("Kajsas hus");
    assertThat(parsedMessage.gateway).isNotNull();
    assertThat(parsedMessage.gateway.id).isEqualTo("12031979");
    assertThat(parsedMessage.gateway.productModel).isEqualTo("CMi2110");
    assertThat(parsedMessage.meter.id).isEqualTo("654");
    assertThat(parsedMessage.meter.medium).isEqualTo("Room sensor");
    assertThat(parsedMessage.meter.manufacturer).isEqualTo("ELV");
    assertThat(parsedMessage.meter.cron).isEqualTo(CRON_FIFTEEN_MINUTES);
    assertThat(parsedMessage.organisationId).isEqualTo("Anka AB");
    assertThat(parsedMessage.sourceSystemId).isEqualTo("AAB");
  }

  @Test
  public void parseMalformedReferenceInfoMessage() {
    assertThat(messageParser.parseReferenceInfoMessage("")).isEmpty();
    assertThat(messageParser.parseReferenceInfoMessage("{\"foo\": 1999}")).isEmpty();
    assertThat(messageParser.parseReferenceInfoMessage("}}}}}}}}}}}}[]]}}}}}}}}}}¡")).isEmpty();
  }

  @Test
  public void missingGateway() {
    String missingGateway = parseJsonFile("messages/reference-info-missing-gateway.json");
    assertThat(messageParser.parseReferenceInfoMessage(missingGateway)).isPresent();
  }

  @Test
  public void missingMeter() {
    String missingMeter = parseJsonFile("messages/reference-info-missing-meter.json");
    assertThat(messageParser.parseReferenceInfoMessage(missingMeter)).isPresent();
  }

  @Test
  public void missingMeterAndGateway() {
    String message = parseJsonFile("messages/reference-info-missing-meter-and-gateway.json");
    assertThat(messageParser.parseReferenceInfoMessage(message)).isPresent();
  }

  @Test
  public void emptyGateway() {
    String emptyGateway = parseJsonFile("messages/reference-info-empty-gateway.json");
    assertThat(messageParser.parseReferenceInfoMessage(emptyGateway)).isPresent();
  }

  @Test
  public void emptyMeter() {
    String emptyMeter = parseJsonFile("messages/reference-info-empty-meter.json");
    assertThat(messageParser.parseReferenceInfoMessage(emptyMeter)).isPresent();
  }

  @Test
  public void readIntervalCanBeNull() {
    String message = parseJsonFile("messages/reference-info-message-no-cron.json");

    MeteringReferenceInfoMessageDto parsedMessage = messageParser
      .parseReferenceInfoMessage(message)
      .get();

    assertThat(parsedMessage.meter.cron).isNull();
  }
}
