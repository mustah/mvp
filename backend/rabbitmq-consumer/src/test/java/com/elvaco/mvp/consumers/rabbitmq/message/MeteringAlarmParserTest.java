package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;

import com.elvaco.mvp.consumers.rabbitmq.dto.MessageType;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class MeteringAlarmParserTest {

  @Test
  public void meteringAlarmMessageIsParsedCorrectly() {
    String jsonMessage = "{\n"
      + "  \"message_type\": \"Elvaco MVP MQ Alarm Message 1.0\",\n"
      + "  \"organisation_id\": \"Organisation, Incorporated\",\n"
      + "  \"source_system_id\": \"The Source System\",\n"
      + "  \"facility\": {\n"
      + "    \"id\": \"ABC-123\"\n"
      + "  },\n"
      + "  \"gateway\": {\n"
      + "    \"id\": \"12031925\"\n"
      + "  },\n"
      + "  \"meter\": {\n"
      + "    \"id\": \"1\"\n"
      + "  },\n"
      + "  \"alarm\": [\n"
      + "   {\n"
      + "     \"timestamp\": \"2017-09-22T08:45:49\",\n"
      + "     \"code\": 42\n"
      + "   }\n"
      + "  ]\n"
      + " }\n";
    MeteringMessageParser messageParser = new MeteringMessageParser();

    MeteringAlarmMessageDto parsedMessage =
      messageParser.parseAlarmMessage(jsonMessage).orElse(null);

    assertThat(parsedMessage).isNotNull();
    assertThat(parsedMessage.organisationId).isEqualTo("Organisation, Incorporated");
    assertThat(parsedMessage.sourceSystemId).isEqualTo("The Source System");
    assertThat(parsedMessage.messageType).isEqualTo(MessageType.METERING_ALARM_V_1_0);
    assertThat(parsedMessage.facility).isNotNull();
    assertThat(parsedMessage.facility.id).isEqualTo("ABC-123");
    assertThat(parsedMessage.gateway).isNotNull();
    assertThat(parsedMessage.gateway.id).isEqualTo("12031925");
    assertThat(parsedMessage.meter).isNotNull();
    assertThat(parsedMessage.meter.id).isEqualTo("1");
    assertThat(parsedMessage.alarm).isNotNull();
    assertThat(parsedMessage.alarm.size()).isEqualTo(1);
    assertThat(parsedMessage.alarm.get(0).timestamp)
      .isEqualTo(LocalDateTime.parse("2017-09-22T08:45:49"));
    assertThat(parsedMessage.alarm.get(0).code).isEqualTo(42);
  }

  @Test
  public void parseMalformedStructureMessage() {
    MeteringMessageParser messageParser = new MeteringMessageParser();
    assertThat(messageParser.parseStructureMessage("")).isEmpty();
    assertThat(messageParser.parseStructureMessage("{\"foo\": 1999}")).isEmpty();
    assertThat(messageParser.parseStructureMessage("}}}}}}}}}}}}[]]}}}}}}}}}}¡")).isEmpty();
    String jsonMessageMissingGateway = "{\n"
      + "  \"message_type\": \"Elvaco MVP MQ Alarm Message 1.0\",\n"
      + "  \"organisation_id\": \"Organisation, Incorporated\",\n"
      + "  \"source_system_id\": \"The Source System\",\n"
      + "  \"facility\": {\n"
      + "    \"id\": \"ABC-123\"\n"
      + "  },\n"
      + "  \"meter\": {\n"
      + "    \"id\": \"1\"\n"
      + "  },\n"
      + "  \"alarm\": [\n"
      + "   {\n"
      + "     \"timestamp\": \"2017-09-22T08:45:49\",\n"
      + "     \"code\": 42\n"
      + "   }\n"
      + "  ]\n"
      + " }\n";
    assertThat(messageParser.parseStructureMessage(jsonMessageMissingGateway)).isEmpty();
  }

}
