package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Collections;
import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringStructureMessageDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeteringMessageListenerTest {

  private MessageConsumerSpy messageConsumerSpy;
  private StructureMessageConsumerSpy structureMessageConsumerSpy;
  private MessageListener messageListener;

  @Before
  public void setUp() {
    messageConsumerSpy = new MessageConsumerSpy();
    structureMessageConsumerSpy = new StructureMessageConsumerSpy();
    messageListener = new MeteringMessageListener(
      new MeteringMessageParser(),
      messageConsumerSpy,
      structureMessageConsumerSpy
    );
  }

  @Test
  public void receiveEmptyMessage() {
    assertThatThrownBy(() -> messageListener.onMessage(""))
      .hasMessageContaining("Failed to parse:");

    assertThat(messageConsumerSpy.messageReceived).isFalse();
  }

  @Test
  public void receiveLongMalformedMessage() {
    String longMessage = String.join("", Collections.nCopies(1000, "x"));

    assertThatThrownBy(() -> messageListener.onMessage(longMessage))
      .hasMessageMatching("Failed to parse:xxxxxxxxxxxxxxxxxxxxx...");

    assertThat(messageConsumerSpy.messageReceived).isFalse();
  }

  @Test
  public void receiveMessageOfUnknownType() {
    String measurementMessage =
      ("{\n"
       + "  \"message_type\": \"Some unknown, unsupported message type\",\n"
       + "}");

    assertThatThrownBy(() -> messageListener.onMessage(measurementMessage))
      .hasMessageContaining("message_type\": \"...");
  }

  @Test
  public void receiveStructureMessage() {
    String message =
      ("{\n"
       + "  \"message_type\": \"Elvaco MVP MQ Reference Info Message 1.0\",\n"
       + "  \"facility\": {\n"
       + "    \"id\": \"ABC-123\",\n"
       + "    \"country\": \"Zimbabwe\",\n"
       + "    \"city\": \"Harare\",\n"
       + "    \"address\": \"Duv\"\n"
       + "  },\n"
       + "  \"gateway\": {\n"
       + "    \"id\": \"12031925\",\n"
       + "    \"product_model\": \"CMi2110\",\n"
       + "    \"status\": \"OK\"\n"
       + "  },\n"
       + "  \"meter\": {\n"
       + "    \"id\": \"1\",\n"
       + "    \"medium\": \"Heat, Return temp\",\n"
       + "    \"status\": \"ERROR\",\n"
       + "    \"manufacturer\": \"ELV\",\n"
       + "    \"expectedInterval\": 15\n"
       + "  },\n"
       + "  \"organisation_id\": \"Organisation, Incorporated\",\n"
       + "  \"source_system_id\": \"The Source System\"\n"
       + "}\n");

    messageListener.onMessage(message);

    assertThat(structureMessageConsumerSpy.messageReceived).isTrue();
    assertThat(messageConsumerSpy.messageReceived).isFalse();
  }

  // TODO[!must!] enable when alarms are implemented
  @Ignore
  @Test
  public void receiveAlarmMessage() {
    String message =
      ("{\n"
       + "\"alarm\": [\n"
       + "\t{\n"
       + "\t\t\"code\": 1308,\n"
       + "\t\t\"description\": \"Elvaco specialfel 3\",\n"
       + "\t\t\"timestamp\": \"2018-03-15T09:00:00\"\n"
       + "\t}\n"
       + "],\n"
       + "\"facility\": {\n"
       + "\t\"id\": \"MVP_alarm_test\"\n"
       + "},\n"
       + "\"gateway\": {\n"
       + "\t\"id\": \"12100016\"\n"
       + "},\n"
       + "\"message_type\": \"Elvaco MVP MQ Alarm Message 1.0\",\n"
       + "\"meter\": {\n"
       + "\t\"id\": \"67125944\"\n"
       + "},\n"
       + "\"organisation_id\": \"Elvaco AB\",\n"
       + "\"source_system_id\": \"Elvaco Metering\"\n"
       + "}\n");

    messageListener.onMessage(message);

    assertThat(messageConsumerSpy.messageReceived).isFalse();
    assertThat(structureMessageConsumerSpy.messageReceived).isFalse();
  }

  @Test
  public void receiveMeasurementMessage() {
    String measurementMessage =
      ("{\n"
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
       + "      \"quantity\": \"power\"\n"
       + "    }\n"
       + "  ]\n"
       + "}");

    messageListener.onMessage(measurementMessage);

    assertThat(messageConsumerSpy.messageReceived).isTrue();
    assertThat(structureMessageConsumerSpy.messageReceived).isFalse();
  }

  private static class MessageConsumerSpy implements MeasurementMessageConsumer {

    private boolean messageReceived;

    @Override
    public Optional<GetReferenceInfoDto> accept(MeteringMeasurementMessageDto message) {
      messageReceived = true;
      return Optional.empty();
    }
  }

  private static class StructureMessageConsumerSpy
    implements StructureMessageConsumer {

    private boolean messageReceived;

    @Override
    public void accept(MeteringStructureMessageDto message) {
      messageReceived = true;
    }
  }
}
