package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class MeteringStructureParserTest {

  @Test
  public void meteringStructureMessageIsParsedCorrectly() {
    String jsonMessage = "{\n"
      + "  \"message_type\": \"Elvaco MVP MQ Reference Info Message 1.0\",\n"
      + "  \"facility\": {\n"
      + "    \"id\": \"ABC-123\",\n"
      + "    \"country\": \"Sweden\",\n"
      + "    \"city\": \"Perstorp\",\n"
      + "    \"address\": \"Duvstigen 8C\"\n"
      + "  },\n"
      + "  \"gateway\": {\n"
      + "    \"id\": \"12031925\",\n"
      + "    \"product_model\": \"CMi2110\",\n"
      + "    \"status\": \"OK\"\n"
      + "  },\n"
      + "  \"meter\": {\n"
      + "    \"id\": \"1\",\n"
      + "    \"medium\": \"Heat, Return temp\",\n"
      + "    \"manufacturer\": \"ELV\",\n"
      + "    \"status\": \"OK\",\n"
      + "    \"expected_interval\": 15\n"
      + "  },\n"
      + "  \"organisation_id\": \"Organisation, Incorporated\",\n"
      + "  \"source_system_id\": \"The Source System\"\n"
      + "}\n";
    MeteringMessageParser messageParser = new MeteringMessageParser();

    MeteringMeterStructureMessageDto parsedMessage =
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
    assertThat(parsedMessage.meter.expectedInterval).isEqualTo(15);
    assertThat(parsedMessage.organisationId).isEqualTo("Organisation, Incorporated");
    assertThat(parsedMessage.sourceSystemId).isEqualTo("The Source System");
  }

  @Test
  public void parseMalformedStructureMessage() {
    MeteringMessageParser messageParser = new MeteringMessageParser();
    assertThat(messageParser.parseStructureMessage("")).isEmpty();
    assertThat(messageParser.parseStructureMessage("{\"foo\": 1999}")).isEmpty();
    assertThat(messageParser.parseStructureMessage("}}}}}}}}}}}}[]]}}}}}}}}}}¡")).isEmpty();
    String jsonMessageMissingGateway = "{\n"
      + "  \"message_type\": \"Elvaco MVP MQ Reference Info Message 1.0\",\n"
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
      + "  \"expectedInterval\": 15\n"
      + "}\n";
    assertThat(messageParser.parseStructureMessage(jsonMessageMissingGateway)).isEmpty();
  }

}
