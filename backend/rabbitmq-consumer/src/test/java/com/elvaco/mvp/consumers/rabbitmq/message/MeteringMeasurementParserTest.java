package com.elvaco.mvp.consumers.rabbitmq.message;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MeteringMeasurementParserTest {

  private MeteringMessageParser messageParser;

  @Before
  public void setUp() {
    messageParser = new MeteringMessageParser();
  }

  @Test
  public void meteringMeasurementMessageIsParsedCorrectly() {
    String jsonMessage =
      "{\n"
      + "  \"message_type\": \"Elvaco MVP MQ Measurement Message 1.0\",\n"
      + "  \"gateway\": {\n"
      + "    \"id\": \"GW-CME3100-XXYYZZ\"\n"
      + "  },\n"
      + "  \"meter\": {\n"
      + "    \"id\": \"123456789\"\n"
      + "  },\n"
      + "  \"facility\": {\n"
      + "    \"id\": \"42402519\"\n"
      + "  },\n"
      + "  \"organisation_id\": \"Elvaco AB\",\n"
      + "  \"source_system_id\": \"Elvaco Metering\",\n"
      + "  \"values\": [\n"
      + "    {\n"
      + "      \"timestamp\": \"2018-03-16T13:07:01\",\n"
      + "      \"value\": 0.659,\n"
      + "      \"unit\": \"wH\",\n"
      + "      \"quantity\": \"power\",\n"
      + "      \"status\": \"OK\"\n"
      + "    }\n"
      + "  ]\n"
      + "}";
    MeteringMeasurementMessageDto parsedMessage =
      messageParser.parseMeasurementMessage(jsonMessage).orElse(null);

    assertThat(parsedMessage).isNotNull();
    assertThat(parsedMessage.messageType).isEqualTo(MessageType.METERING_MEASUREMENT_V_1_0);
    assertThat(parsedMessage.gateway).isNotNull();
    assertThat(parsedMessage.gateway.id).isEqualTo("GW-CME3100-XXYYZZ");
    assertThat(parsedMessage.meter).isNotNull();
    assertThat(parsedMessage.meter.id).isEqualTo("123456789");
    assertThat(parsedMessage.facility).isNotNull();
    assertThat(parsedMessage.facility.id).isEqualTo("42402519");
    assertThat(parsedMessage.organisationId).isEqualTo("Elvaco AB");
    assertThat(parsedMessage.sourceSystemId).isEqualTo("Elvaco Metering");
    assertThat(parsedMessage.values).isNotNull();
    assertThat(parsedMessage.values).hasSize(1);
    ValueDto value = parsedMessage.values.get(0);
    assertThat(value.timestamp).isEqualTo(LocalDateTime.parse("2018-03-16T13:07:01"));
    assertThat(value.value).isEqualTo(0.659);
    assertThat(value.unit).isEqualTo("wH");
    assertThat(value.quantity).isEqualTo("power");
  }

  @Test
  public void extraneousFieldsAreOk() {
    String jsonMessage =
      "{\n"
        + "  \"message_type\": \"Elvaco MVP MQ Measurement Message 1.0\",\n"
        + "  \"gateway\": {\n"
        + "    \"id\": \"GW-CME3100-XXYYZZ\"\n"
        + "  },\n"
        + "  \"meter\": {\n"
        + "    \"id\": \"123456789\"\n"
        + "  },\n"
        + "  \"facility\": {\n"
        + "    \"id\": \"42402519\"\n"
        + "  },\n"
        + "  \"organisation_id\": \"Elvaco AB\",\n"
        + "  \"source_system_id\": \"Elvaco Metering\",\n"
        + "  \"values\": [\n"
        + "    {\n"
        + "      \"timestamp\": \"2018-03-16T13:07:01\",\n"
        + "      \"value\": 0.659,\n"
        + "      \"unit\": \"wH\",\n"
        + "      \"quantity\": \"power\",\n"
        + "      \"status\": \"OK\",\n"
        + "      \"extra\": 3\n"
        + "    }\n"
        + "  ]\n"
        + "}";
    MeteringMeasurementMessageDto parsedMessage =
      messageParser.parseMeasurementMessage(jsonMessage).orElse(null);

    assertThat(parsedMessage).isNotNull();
    assertThat(parsedMessage.messageType).isEqualTo(MessageType.METERING_MEASUREMENT_V_1_0);
    assertThat(parsedMessage.gateway).isNotNull();
    assertThat(parsedMessage.gateway.id).isEqualTo("GW-CME3100-XXYYZZ");
    assertThat(parsedMessage.meter).isNotNull();
    assertThat(parsedMessage.meter.id).isEqualTo("123456789");
    assertThat(parsedMessage.facility).isNotNull();
    assertThat(parsedMessage.facility.id).isEqualTo("42402519");
    assertThat(parsedMessage.organisationId).isEqualTo("Elvaco AB");
    assertThat(parsedMessage.sourceSystemId).isEqualTo("Elvaco Metering");
    assertThat(parsedMessage.values).isNotNull();
    assertThat(parsedMessage.values).hasSize(1);
    ValueDto value = parsedMessage.values.get(0);
    assertThat(value.timestamp).isEqualTo(LocalDateTime.parse("2018-03-16T13:07:01"));
    assertThat(value.value).isEqualTo(0.659);
    assertThat(value.unit).isEqualTo("wH");
    assertThat(value.quantity).isEqualTo("power");
  }

  @Test
  public void measurementMessageMissingGatewayIsOk() {
    String jsonMessage =
      "{\n"
      + "  \"message_type\": \"Elvaco MVP MQ Measurement Message 1.0\",\n"
      + "  \"meter\": {\n"
      + "    \"id\": \"123456789\"\n"
      + "  },\n"
      + "  \"facility\": {\n"
      + "    \"id\": \"42402519\"\n"
      + "  },\n"
      + "  \"organisation_id\": \"Elvaco AB\",\n"
      + "  \"source_system_id\": \"Elvaco Metering\",\n"
      + "  \"values\": [\n"
      + "    {\n"
      + "      \"timestamp\": \"2018-03-16T13:07:01\",\n"
      + "      \"value\": 0.659,\n"
      + "      \"unit\": \"wH\",\n"
      + "      \"quantity\": \"power\",\n"
      + "      \"status\": \"OK\"\n"
      + "    }\n"
      + "  ]\n"
      + "}";

    MeteringMeasurementMessageDto parsedMessage =
      messageParser.parseMeasurementMessage(jsonMessage).get();

    assertThat(parsedMessage.gateway).isNull();
  }

  @Test
  public void parseMalformedMeasurementMessage() {
    assertThat(messageParser.parseMeasurementMessage("")).isEmpty();
    assertThat(messageParser.parseMeasurementMessage("{\"foo\": 1999}")).isEmpty();
    assertThat(messageParser.parseMeasurementMessage("}}}}}}}}}}}}[]]}}}}}}}}}}¡")).isEmpty();

    String jsonMessageWithoutValues =
      "{\n"
      + "  \"message_type\": \"Elvaco MVP MQ Measurement Message 1.0\",\n"
      + "  \"gateway\": {\n"
      + "    \"id\": \"GW-CME3100-XXYYZZ\"\n"
      + "  },\n"
      + "  \"meter\": {\n"
      + "    \"id\": \"123456789\"\n"
      + "  },\n"
      + "  \"facility\": {\n"
      + "    \"id\": \"42402519\"\n"
      + "  },\n"
      + "  \"organisation_id\": \"Elvaco AB\",\n"
      + "  \"source_system_id\": \"Elvaco Metering\"\n"
      + "}";
    assertThat(messageParser.parseMeasurementMessage(jsonMessageWithoutValues)).isEmpty();
  }

  @Test
  public void parseMeasurementValuesWithReturnTemperatureAndUnitMapping() {
    String message = parseJsonFile("messages/measurements-return-temp.json");

    MeteringMeasurementMessageDto parsedMessage =
      messageParser.parseMeasurementMessage(message).get();

    assertThat(parsedMessage.values).containsExactly(
      new ValueDto(LocalDateTime.parse("2018-03-16T13:07:01"), 0.659, "°C", "Return temp."),
      new ValueDto(LocalDateTime.parse("2018-03-16T14:07:01"), 0.759, "°C", "Return temp."),
      new ValueDto(LocalDateTime.parse("2018-03-16T15:07:01"), 37.4, "°C", "Return temp.")
    );
  }

  private String parseJsonFile(String file) {
    return new JsonParser()
      .parse(new JsonReader(new InputStreamReader(loadResource(file), Charset.forName("UTF-8"))))
      .getAsJsonObject()
      .toString();
  }

  private static InputStream loadResource(String file) {
    return MeteringMeasurementParserTest.class.getClassLoader().getResourceAsStream(file);
  }

}
