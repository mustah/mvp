package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Collections;
import java.util.Optional;

import com.elvaco.mvp.consumers.rabbitmq.dto.InfrastructureStatusMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.core.util.MessageThrottler;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import com.elvaco.mvp.testing.cache.MockCache;

import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.consumers.rabbitmq.message.JsonFileReader.parseJsonFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeteringMessageListenerTest {

  private MessageConsumerSpy messageConsumerSpy;
  private ReferenceInfoMessageConsumerSpy referenceInfoMessageConsumerSpy;
  private AlarmMessageConsumerSpy alarmMessageConsumerSpy;
  private InfrastructureStatusMessageConsumerSpy infrastructureStatusMessageConsumerSpy;
  private MessageListener messageListener;

  @Before
  public void setUp() {
    messageConsumerSpy = new MessageConsumerSpy();
    referenceInfoMessageConsumerSpy = new ReferenceInfoMessageConsumerSpy();
    alarmMessageConsumerSpy = new AlarmMessageConsumerSpy();
    infrastructureStatusMessageConsumerSpy = new InfrastructureStatusMessageConsumerSpy();
    messageListener = new MeteringMessageListener(
      new MeteringMessageParser(),
      messageConsumerSpy,
      referenceInfoMessageConsumerSpy,
      alarmMessageConsumerSpy,
      infrastructureStatusMessageConsumerSpy,
      new MessageThrottler<>(new MockCache<>(), String::valueOf)
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
  public void receiveReferenceInfoMessage() {
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

    assertThat(referenceInfoMessageConsumerSpy.messageReceived).isTrue();
    assertThat(messageConsumerSpy.messageReceived).isFalse();
    assertThat(infrastructureStatusMessageConsumerSpy.messageReceived).isFalse();
  }

  @Test
  public void receiveAlarmMessage() {
    String message = parseJsonFile("messages/alarm.json");

    messageListener.onMessage(message);

    assertThat(alarmMessageConsumerSpy.messageReceived).isTrue();
    assertThat(messageConsumerSpy.messageReceived).isFalse();
    assertThat(referenceInfoMessageConsumerSpy.messageReceived).isFalse();
    assertThat(infrastructureStatusMessageConsumerSpy.messageReceived).isFalse();
  }

  @Test
  public void receiveInfrastructureStatusMessage() {
    String message = parseJsonFile("messages/infrastructure-status.json");

    messageListener.onMessage(message);

    assertThat(alarmMessageConsumerSpy.messageReceived).isFalse();
    assertThat(messageConsumerSpy.messageReceived).isFalse();
    assertThat(referenceInfoMessageConsumerSpy.messageReceived).isFalse();
    assertThat(infrastructureStatusMessageConsumerSpy.messageReceived).isTrue();
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
    assertThat(referenceInfoMessageConsumerSpy.messageReceived).isFalse();
    assertThat(infrastructureStatusMessageConsumerSpy.messageReceived).isFalse();
  }

  @Test
  public void repeatedMessagesAreThrottled() {
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

    assertThat(messageListener.onMessage(measurementMessage)).isNotNull();
    assertThat(messageListener.onMessage(measurementMessage)).isNull();
  }

  private static class MessageConsumerSpy implements MeasurementMessageConsumer {

    private boolean messageReceived;

    @Override
    public Optional<GetReferenceInfoDto> accept(MeteringMeasurementMessageDto message) {
      messageReceived = true;
      return Optional.of(
        new GetReferenceInfoDto(
          message.organisationId,
          "job-1234",
          new MeterIdDto(message.meter.id),
          message.gateway().map(gatewayIdDto -> new GatewayIdDto(gatewayIdDto.id)).orElse(null),
          new FacilityIdDto(message.facility.id)
        )
      );
    }
  }

  private static class ReferenceInfoMessageConsumerSpy
    implements ReferenceInfoMessageConsumer {

    private boolean messageReceived;

    @Override
    public void accept(MeteringReferenceInfoMessageDto message) {
      messageReceived = true;
    }
  }

  private static class AlarmMessageConsumerSpy
    implements AlarmMessageConsumer {

    private boolean messageReceived;

    @Override
    public Optional<GetReferenceInfoDto> accept(MeteringAlarmMessageDto message) {
      messageReceived = true;
      return Optional.empty();
    }
  }

  private static class InfrastructureStatusMessageConsumerSpy
    implements InfrastructureMessageConsumer {

    private boolean messageReceived;

    @Override
    public void accept(InfrastructureStatusMessageDto message) {
      messageReceived = true;
    }
  }
}
