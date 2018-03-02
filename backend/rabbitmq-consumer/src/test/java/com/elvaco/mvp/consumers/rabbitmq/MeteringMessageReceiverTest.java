package com.elvaco.mvp.consumers.rabbitmq;

import java.util.Collections;

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
      + "}\n").getBytes();
    meteringMessageReceiver.receiveMessage(structureMessage);

    assertThat(messageHandler.structureMessageReceived).isTrue();
    assertThat(messageHandler.measurementMessageReceived).isFalse();
  }


  @Test
  public void receiveMeasurementMessage() {
    MessageHandlerSpy messageHandler = new MessageHandlerSpy();
    MeteringMessageReceiver meteringMessageReceiver = new MeteringMessageReceiver(messageHandler);
    byte[] measurementMessage = ("{\n"
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
      + "}").getBytes();
    meteringMessageReceiver.receiveMessage(measurementMessage);

    assertThat(messageHandler.measurementMessageReceived).isTrue();
    assertThat(messageHandler.structureMessageReceived).isFalse();
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

    MessageHandlerSpy() {
      structureMessageReceived = false;
      measurementMessageReceived = false;
    }

    @Override
    public void handle(MeteringMeterStructureMessageDto structureMessage) {
      structureMessageReceived = true;
    }

    @Override
    public void handle(MeteringMeasurementMessageDto measurementMessage) {
      measurementMessageReceived = true;
    }

    boolean nothingReceived() {
      return !(structureMessageReceived || measurementMessageReceived);
    }
  }


}
