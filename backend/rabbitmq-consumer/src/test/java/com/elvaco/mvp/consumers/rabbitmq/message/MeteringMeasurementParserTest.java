package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;

import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.consumers.rabbitmq.message.JsonFileReader.parseJsonFile;
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
    assertThat(parsedMessage.gateway().get().id).isEqualTo("GW-CME3100-XXYYZZ");
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
    assertThat(parsedMessage.gateway().get().id).isEqualTo("GW-CME3100-XXYYZZ");
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

    assertThat(parsedMessage.gateway()).isNotPresent();
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

  @Test
  public void parseWithEmptyGatewayField() {
    String message = parseJsonFile("messages/measurements-empty-gateway-field.json");

    MeteringMeasurementMessageDto parsedMessage =
      messageParser.parseMeasurementMessage(message).get();

    assertThat(parsedMessage.gateway()).isNotPresent();
  }

  @Test
  public void unitsAreTranslatedFromMetering() {
    String jsonMessage =
      "\n"
      + "\n"
      + "{\n"
      + "  \"message_type\": \"Elvaco MVP MQ Measurement Message 1.0\",\n"
      + "  \"organisation_id\": \"Elvaco AB\",\n"
      + "  \"source_system_id\": \"Elvaco Metering\",\n"
      + "  \"jobId\": \"\",\n"
      + "  \"facility\": {\n"
      + "    \"id\": \"43108\"\n"
      + "  },\n"
      + "  \"gateway\": {\n"
      + "    \"id\": \"12001058\"\n"
      + "  },\n"
      + "  \"meter\": {\n"
      + "    \"id\": \"28253\"\n"
      + "  },\n"
      + "  \"values\": [\n"
      + "    {\n"
      + "      \"timestamp\": \"2018-03-28T00:00:00\",\n"
      + "      \"value\": 98.721,\n"
      + "      \"unit\": \"MWh\",\n"
      + "      \"quantity\": \"Energy\",\n"
      + "      \"status\": \"OK\"\n"
      + "    },\n"
      + "    {\n"
      + "      \"timestamp\": \"2018-03-28T00:00:00\",\n"
      + "      \"value\": 3089.2,\n"
      + "      \"unit\": \"m^3\",\n"
      + "      \"quantity\": \"Volume\",\n"
      + "      \"status\": \"OK\"\n"
      + "    },\n"
      + "    {\n"
      + "      \"timestamp\": \"2018-03-28T00:00:00\",\n"
      + "      \"value\": 4.2,\n"
      + "      \"unit\": \"kW\",\n"
      + "      \"quantity\": \"Power\",\n"
      + "      \"status\": \"OK\"\n"
      + "    },\n"
      + "    {\n"
      + "      \"timestamp\": \"2018-03-28T00:00:00\",\n"
      + "      \"value\": 0.135,\n"
      + "      \"unit\": \"m^3\\/h\",\n"
      + "      \"quantity\": \"Volume flow\",\n"
      + "      \"status\": \"OK\"\n"
      + "    },\n"
      + "    {\n"
      + "      \"timestamp\": \"2018-03-28T00:00:00\",\n"
      + "      \"value\": 82,\n"
      + "      \"unit\": \"Celsius\",\n"
      + "      \"quantity\": \"Flow temp.\",\n"
      + "      \"status\": \"OK\"\n"
      + "    },\n"
      + "    {\n"
      + "      \"timestamp\": \"2018-03-28T00:00:00\",\n"
      + "      \"value\": 55,\n"
      + "      \"unit\": \"Celsius\",\n"
      + "      \"quantity\": \"Return temp.\",\n"
      + "      \"status\": \"OK\"\n"
      + "    },\n"
      + "    {\n"
      + "      \"timestamp\": \"2018-03-28T00:00:00\",\n"
      + "      \"value\": 27.1,\n"
      + "      \"unit\": \"Kelvin\",\n"
      + "      \"quantity\": \"Difference temp.\",\n"
      + "      \"status\": \"OK\"\n"
      + "    },\n"
      + "    {\n"
      + "      \"timestamp\": \"2018-03-28T00:00:00\",\n"
      + "      \"value\": 99,\n"
      + "      \"unit\": \"m3\",\n"
      + "      \"quantity\": \"Volume\",\n"
      + "      \"status\": \"OK\"\n"
      + "    },\n"
      + "    {\n"
      + "      \"timestamp\": \"2018-03-28T00:00:00\",\n"
      + "      \"value\": 16,\n"
      + "      \"unit\": \"m3/h\",\n"
      + "      \"quantity\": \"Volume Flow\",\n"
      + "      \"status\": \"OK\"\n"
      + "    }\n"
      + "  ]\n"
      + "}\n"
      + "\n";
    MeteringMeasurementMessageDto parsedMessage = messageParser
      .parseMeasurementMessage(jsonMessage).get();

    LocalDateTime expectedTimestamp = LocalDateTime.parse("2018-03-28T00:00:00");

    assertThat(parsedMessage.values).containsExactly(
      new ValueDto(expectedTimestamp, 98.721, "MWh", "Energy"),
      new ValueDto(expectedTimestamp, 3089.2, "m^3", "Volume"),
      new ValueDto(expectedTimestamp, 4.2, "kW", "Power"),
      new ValueDto(expectedTimestamp, 0.135, "m^3/h", "Volume flow"),
      new ValueDto(expectedTimestamp, 82, "°C", "Flow temp."),
      new ValueDto(expectedTimestamp, 55, "°C", "Return temp."),
      new ValueDto(expectedTimestamp, 27.1, "K", "Difference temp."),
      new ValueDto(expectedTimestamp, 99, "m³", "Volume"),
      new ValueDto(expectedTimestamp, 16, "m³/h", "Volume Flow")
    );
  }
}
