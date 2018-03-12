package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.consumers.rabbitmq.dto.AlarmDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class MeteringMessageParserTest {

  @Test
  public void meteringStructureMessageIsParsedCorrectly() {
    String jsonMessage = "{\n"
      + "  \"message_type\": \"Elvaco MVP MQ Structure Message 1.0\",\n"
      + "  \"facility_id\": \"ABC-123\",\n"
      + "  \"gateway\": {\n"
      + "    \"id\": \"12031925\",\n"
      + "    \"product_model\": \"CMi2110\"\n"
      + "  },\n"
      + "  \"meter_id\": \"1\",\n"
      + "  \"medium\": \"Heat, Return temp\",\n"
      + "  \"location\": {\n"
      + "    \"country\": \"Sweden\",\n"
      + "    \"city\": \"Perstorp\",\n"
      + "    \"address\": \"Duvstigen 8C\"\n"
      + "  },\n"
      + "  \"manufacturer\": \"ELV\",\n"
      + "  \"organisation_id\": \"Organisation, Incorporated\",\n"
      + "  \"source_system_id\": \"The Source System\",\n"
      + "  \"expected_interval\": 15\n"
      + "}\n";
    MeteringMessageParser messageParser = new MeteringMessageParser();

    MeteringMeterStructureMessageDto parsedMessage =
      messageParser.parseStructureMessage(jsonMessage).orElse(null);

    assertThat(parsedMessage).isNotNull();
    assertThat(parsedMessage.messageType).isEqualTo(MessageType.METERING_METER_STRUCTURE_V_1_0);
    assertThat(parsedMessage.facilityId).isEqualTo("ABC-123");
    assertThat(parsedMessage.gateway).isNotNull();
    assertThat(parsedMessage.gateway.id).isEqualTo("12031925");
    assertThat(parsedMessage.gateway.productModel).isEqualTo("CMi2110");
    assertThat(parsedMessage.meterId).isEqualTo("1");
    assertThat(parsedMessage.medium).isEqualTo("Heat, Return temp");
    assertThat(parsedMessage.location).isNotNull();
    assertThat(parsedMessage.location.country).isEqualTo("Sweden");
    assertThat(parsedMessage.location.city).isEqualTo("Perstorp");
    assertThat(parsedMessage.location.address).isEqualTo("Duvstigen 8C");
    assertThat(parsedMessage.manufacturer).isEqualTo("ELV");
    assertThat(parsedMessage.organisationId).isEqualTo("Organisation, Incorporated");
    assertThat(parsedMessage.sourceSystemId).isEqualTo("The Source System");
    assertThat(parsedMessage.expectedInterval).isEqualTo(15);
  }

  @Test
  public void meteringMeasurementMessageIsParsedCorrectly() {
    String jsonMessage = "{\n"
      + "  \"message_type\": \"Elvaco MVP MQ Measurement Message 1.0\",\n"
      + "  \"gateway\": {\n"
      + "    \"id\": \"GW-CME3100-XXYYZZ\",\n"
      + "    \"status\": \"OK\"\n"
      + "  },\n"
      + "  \"meter\": {\n"
      + "    \"id\": \"123456789\",\n"
      + "    \"status\": \"ERROR\"\n"
      + "  },\n"
      + "  \"facility_id\": \"42402519\",\n"
      + "  \"organisation_id\": \"Elvaco AB\",\n"
      + "  \"source_system_id\": \"Elvaco Metering\",\n"
      + "  \"values\": [\n"
      + "    {\n"
      + "      \"timestamp\": 1506069947,\n"
      + "      \"value\": 0.659,\n"
      + "      \"unit\": \"wH\",\n"
      + "      \"quantity\": \"power\"\n"
      + "    }\n"
      + "  ],\n"
      + "  \"alarms\": [\n"
      + "    {\n"
      + "      \"timestamp\": 1506069947,\n"
      + "      \"code\": 42,\n"
      + "      \"description\": \"Low battery\"\n"
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
    assertThat(parsedMessage.facility).isEqualTo("42402519");
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
  public void parseMalformedStructureMessage() {
    MeteringMessageParser messageParser = new MeteringMessageParser();
    assertThat(messageParser.parseStructureMessage("")).isEmpty();
    assertThat(messageParser.parseStructureMessage("{\"foo\": 1999}")).isEmpty();
    assertThat(messageParser.parseStructureMessage("}}}}}}}}}}}}[]]}}}}}}}}}}¡")).isEmpty();
    String jsonMessageMissingGateway = "{\n"
      + "  \"message_type\": \"Elvaco MVP MQ Structure Message 1.0\",\n"
      + "  \"facility_id\": \"ABC-123\",\n"
      + "  \"meter_id\": \"1\",\n"
      + "  \"medium\": \"Heat, Return temp\",\n"
      + "  \"location\": {\n"
      + "    \"country\": \"Sweden\",\n"
      + "    \"city\": \"Perstorp\",\n"
      + "    \"address\": \"Duvstigen 8C\"\n"
      + "  },\n"
      + "  \"manufacturer\": \"ELV\",\n"
      + "  \"organisation_id\": \"Organisation, Incorporated\",\n"
      + "  \"source_system_id\": \"The Source System\",\n"
      + "  \"expected_interval\": 15\n"
      + "}\n";
    assertThat(messageParser.parseStructureMessage(jsonMessageMissingGateway)).isEmpty();
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
      + "    \"id\": \"GW-CME3100-XXYYZZ\",\n"
      + "    \"status\": \"OK\"\n"
      + "  },\n"
      + "  \"meter\": {\n"
      + "    \"id\": \"123456789\",\n"
      + "    \"status\": \"ERROR\"\n"
      + "  },\n"
      + "  \"facility_id\": \"42402519\",\n"
      + "  \"organisation_id\": \"Elvaco AB\",\n"
      + "  \"source_system_id\": \"Elvaco Metering\",\n"
      + "  \"alarms\": [\n"
      + "    {\n"
      + "      \"timestamp\": 1506069947,\n"
      + "      \"code\": 42,\n"
      + "      \"description\": \"Low battery\"\n"
      + "    }\n"
      + "  ]\n"
      + "}";
    assertThat(messageParser.parseMeasurementMessage(jsonMessageWithoutValues)).isEmpty();
  }
}
