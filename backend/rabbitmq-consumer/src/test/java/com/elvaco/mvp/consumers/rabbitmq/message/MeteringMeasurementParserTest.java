package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class MeteringMeasurementParserTest {

  @Test
  public void meteringMeasurementMessageIsParsedCorrectly() {
    String jsonMessage = "{\n"
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
      + "      \"timestamp\": 1506069947,\n"
      + "      \"value\": 0.659,\n"
      + "      \"unit\": \"wH\",\n"
      + "      \"quantity\": \"power\",\n"
      + "      \"status\": \"OK\"\n"
      + "    }\n"
      + "  ]\n"
      + "}";
    MeteringMessageParser messageParser = new MeteringMessageParser();
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
    assertThat(value.timestamp).isEqualTo(1506069947);
    assertThat(value.value).isEqualTo(0.659);
    assertThat(value.unit).isEqualTo("wH");
    assertThat(value.quantity).isEqualTo("power");
  }

  @Test
  public void parseMalformedMeasurementMessage() {
    MeteringMessageParser messageParser = new MeteringMessageParser();
    assertThat(messageParser.parseMeasurementMessage("")).isEmpty();
    assertThat(messageParser.parseMeasurementMessage("{\"foo\": 1999}")).isEmpty();
    assertThat(messageParser.parseMeasurementMessage("}}}}}}}}}}}}[]]}}}}}}}}}}¡")).isEmpty();

    String jsonMessageWithoutValues = "{\n"
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
}
