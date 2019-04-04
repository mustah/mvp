package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.consumers.rabbitmq.dto.InfrastructureStatusMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.consumers.rabbitmq.message.JsonFileReader.parseJsonFile;
import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.util.Lists.newArrayList;

public class MeteringMessageParserTest {

  private MeteringMessageParser messageParser;

  @Before
  public void setUp() {
    messageParser = new MeteringMessageParser();
  }

  @Test
  public void infrastructureStatus_Valid() {
    String jsonMessage = parseJsonFile("messages/infrastructure-status.json");

    assertThat(messageParser.parseInfrastructureStatusMessage(jsonMessage))
      .isPresent()
      .get()
      .extracting(dto -> newArrayList(((ObjectNode) dto.properties).fieldNames()))
      .asList()
      .containsExactlyInAnyOrder(
        "Message format",
        "Uptime",
        "Timestamp",
        "APN",
        "Network Cell Id",
        "RSSI",
        "SNR",
        "ECL",
        "Network Class",
        "Current 24h",
        "Battery Monitor"
      );
  }

  @Test
  public void infrastructureStatus_MissingPropertyIsLeftOut() {
    String jsonMessage = parseJsonFile("messages/infrastructure-status.json");
    var json = (ObjectNode) toJsonNode(jsonMessage);
    json.remove("MF");

    assertThat(messageParser.parseInfrastructureStatusMessage(json.toString()))
      .isPresent()
      .get()
      .extracting(dto -> newArrayList(((ObjectNode) dto.properties).fieldNames()))
      .asList()
      .doesNotContain("MF", "Message format");
  }

  @Test
  public void infrastructureStatus_UnknownPropertiesAreLeftIn() {
    String jsonMessage = parseJsonFile("messages/infrastructure-status.json");
    var json = (ObjectNode) toJsonNode(jsonMessage);
    json.put("my own key", "my very special value");

    assertThat(messageParser.parseInfrastructureStatusMessage(json.toString()))
      .isPresent()
      .get()
      .extracting(dto -> newArrayList(((ObjectNode) dto.properties).fieldNames()))
      .asList()
      .contains("my own key");
  }

  @Test
  public void parseMessageType_measurement() {
    String jsonMessage = parseJsonFile("messages/measurements.json");
    assertThat(messageParser.parse(jsonMessage))
      .isPresent()
      .get()
      .isInstanceOf(MeteringMeasurementMessageDto.class)
      .extracting(dto -> dto.messageType).isEqualTo(MessageType.METERING_MEASUREMENT_V_1_0);
  }

  @Test
  public void parseMessageType_referenceInfo() {
    String jsonMessage = parseJsonFile("messages/reference-info-valid-gas-meter.json");
    assertThat(messageParser.parse(jsonMessage))
      .isPresent()
      .get()
      .isInstanceOf(MeteringReferenceInfoMessageDto.class)
      .extracting(dto -> dto.messageType).isEqualTo(MessageType.METERING_REFERENCE_INFO_V_1_0);
  }

  @Test
  public void parseMessageType_alarm() {
    String jsonMessage = parseJsonFile("messages/alarm.json");
    assertThat(messageParser.parse(jsonMessage))
      .isPresent()
      .get()
      .isInstanceOf(MeteringAlarmMessageDto.class)
      .extracting(dto -> dto.messageType).isEqualTo(MessageType.METERING_ALARM_V_1_0);
  }

  @Test
  public void parseMessageType_infrastructureStatus() {
    String jsonMessage = parseJsonFile("messages/infrastructure-status.json");
    assertThat(messageParser.parse(jsonMessage))
      .isPresent()
      .get()
      .isInstanceOf(InfrastructureStatusMessageDto.class)
      .extracting(dto -> dto.messageType).isEqualTo(MessageType.INFRASTRUCTURE_STATUS_V_1_0);
  }

  @Test
  public void parseMessageType_infrastructureExtendedStatus() {
    String jsonMessage = parseJsonFile("messages/infrastructure-extended-status.json");
    assertThat(messageParser.parse(jsonMessage))
      .isNotPresent();
  }

  @Test
  public void parseMessageType_standardMessage() {
    String jsonMessage = parseJsonFile("messages/nbiot-standard-message.json");
    assertThat(messageParser.parse(jsonMessage))
      .isNotPresent();
  }

  @Test
  public void parseMessage_otherJson() {
    String jsonMessage = "{\"blabla\":\"babla\"}";
    assertThatThrownBy(() -> messageParser.parse(jsonMessage))
      .isInstanceOf(RuntimeException.class)
      .hasMessageContaining("Failed to parse:");
  }

  @Test
  public void parseMessage_invalidJson() {
    String jsonMessage = "blabla}";
    assertThatThrownBy(() -> messageParser.parse(jsonMessage))
      .isInstanceOf(RuntimeException.class);
  }
}
