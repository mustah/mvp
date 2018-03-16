package com.elvaco.mvp.consumers.rabbitmq;

import java.util.Collections;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeterStructureMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.message.MessageHandler;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeteringMessageReceiverTest {

  @Test
  public void receiveEmptyMessage() {
    MessageHandlerSpy messageHandler = new MessageHandlerSpy();
    MeteringMessageReceiver meteringMessageReceiver = new MeteringMessageReceiver(messageHandler);

    assertThatThrownBy(() -> meteringMessageReceiver.receiveMessage("".getBytes()))
      .hasMessageContaining(
        "Malformed metering message");

    assertThat(messageHandler.nothingReceived()).isTrue();
  }

  @Test
  public void receiveLongMalformedMessage() {
    MessageHandlerSpy messageHandler = new MessageHandlerSpy();
    MeteringMessageReceiver meteringMessageReceiver = new MeteringMessageReceiver(messageHandler);

    byte[] longMessage = String.join("", Collections.nCopies(1000, "x")).getBytes();
    assertThatThrownBy(() -> meteringMessageReceiver.receiveMessage(longMessage))
      .hasMessageMatching(
        "^Malformed metering message: [x]{37}\\.\\.\\.$");

    assertThat(messageHandler.nothingReceived()).isTrue();
  }

  @Test
  public void receiveStructureMessage() {
    MessageHandlerSpy messageHandler = new MessageHandlerSpy();
    MeteringMessageReceiver meteringMessageReceiver = new MeteringMessageReceiver(messageHandler);

    byte[] structureMessage = ("{\n"
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
      + "}\n").getBytes();
    meteringMessageReceiver.receiveMessage(structureMessage);

    assertThat(messageHandler.structureMessageReceived).isTrue();
    assertThat(messageHandler.measurementMessageReceived).isFalse();
    assertThat(messageHandler.alarmMessageReceived).isFalse();
  }

  @Test
  public void receiveAlarmMessage() {
    MessageHandlerSpy messageHandler = new MessageHandlerSpy();
    MeteringMessageReceiver meteringMessageReceiver = new MeteringMessageReceiver(messageHandler);

    byte[] alarmMessage = ("{\n"
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
      + "}\n").getBytes();

    meteringMessageReceiver.receiveMessage(alarmMessage);

    assertThat(messageHandler.measurementMessageReceived).isFalse();
    assertThat(messageHandler.structureMessageReceived).isFalse();
    assertThat(messageHandler.alarmMessageReceived).isTrue();
  }

  @Test
  public void receiveMeasurementMessage() {
    MessageHandlerSpy messageHandler = new MessageHandlerSpy();
    MeteringMessageReceiver meteringMessageReceiver = new MeteringMessageReceiver(messageHandler);
    byte[] measurementMessage = ("{\n"
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
      + "}").getBytes();
    meteringMessageReceiver.receiveMessage(measurementMessage);

    assertThat(messageHandler.measurementMessageReceived).isTrue();
    assertThat(messageHandler.structureMessageReceived).isFalse();
    assertThat(messageHandler.alarmMessageReceived).isFalse();
  }

  @Test
  public void receiveMessageOfUnknownType() {
    MessageHandlerSpy messageHandler = new MessageHandlerSpy();
    MeteringMessageReceiver meteringMessageReceiver = new MeteringMessageReceiver(messageHandler);
    byte[] measurementMessage = ("{\n"
      + "  \"message_type\": \"Some unknown, unsupported message type\",\n"
      + "}").getBytes();

    assertThatThrownBy(() -> meteringMessageReceiver.receiveMessage(measurementMessage))
      .hasMessageContaining(
        "Malformed metering message");
  }

  static class MessageHandlerSpy implements MessageHandler {

    private boolean structureMessageReceived;
    private boolean measurementMessageReceived;
    private boolean alarmMessageReceived;

    MessageHandlerSpy() {
      structureMessageReceived = false;
      measurementMessageReceived = false;
      alarmMessageReceived = false;
    }

    @Override
    public void handle(MeteringMeterStructureMessageDto structureMessage) {
      structureMessageReceived = true;
    }

    @Override
    public void handle(MeteringMeasurementMessageDto measurementMessage) {
      measurementMessageReceived = true;
    }

    @Override
    public void handle(MeteringAlarmMessageDto alarmMessage) {
      alarmMessageReceived = true;
    }

    boolean nothingReceived() {
      return !(structureMessageReceived || measurementMessageReceived || alarmMessageReceived);
    }
  }


}
