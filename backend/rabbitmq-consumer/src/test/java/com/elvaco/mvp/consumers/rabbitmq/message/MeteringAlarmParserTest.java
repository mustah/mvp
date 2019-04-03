package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;

import com.elvaco.mvp.consumers.rabbitmq.dto.AlarmDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MessageType;

import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.consumers.rabbitmq.message.JsonFileReader.parseJsonFile;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class MeteringAlarmParserTest {

  private MeteringMessageParser messageParser;

  @Before
  public void setUp() {
    messageParser = new MeteringMessageParser();
  }

  @Test
  public void malformedAlarmMessage() {
    assertThat(messageParser.parseAlarmMessage("")).isEmpty();
    assertThat(messageParser.parseAlarmMessage("{\"foo\": 1999}")).isEmpty();
    assertThat(messageParser.parseAlarmMessage("}}}}}}}}}}}}[]]}}}}}}}}}}¡")).isEmpty();
  }

  @Test
  public void meteringAlarmMessageIsParsedCorrectly() {
    String message = parseJsonFile("messages/alarm.json");

    MeteringAlarmMessageDto parsedMessage = messageParser.parseAlarmMessage(message).get();

    assertReferenceInfoIsOk(parsedMessage);

    assertThat(parsedMessage.alarm)
      .containsExactly(new AlarmDto(LocalDateTime.parse("2017-09-22T08:45:49"), 42));
  }

  @Test
  public void missingMeter() {
    String message = parseJsonFile("messages/alarm-missing-meter.json");

    assertThat(messageParser.parseAlarmMessage(message)).isEmpty();
  }

  @Test
  public void messageWithTwoAlarms() {
    String message = parseJsonFile("messages/alarm-several.json");

    MeteringAlarmMessageDto parsedMessage = messageParser.parseAlarmMessage(message).get();

    assertReferenceInfoIsOk(parsedMessage);

    assertThat(parsedMessage.alarm)
      .containsExactlyInAnyOrder(
        new AlarmDto(LocalDateTime.parse("2017-09-22T08:45:49"), 42),
        new AlarmDto(LocalDateTime.parse("2018-08-26T08:45:49"), 89)
      );
  }

  private static void assertReferenceInfoIsOk(MeteringAlarmMessageDto parsedMessage) {
    assertThat(parsedMessage.messageType).isEqualTo(MessageType.METERING_ALARM_V_1_0);
    assertThat(parsedMessage.organisationId).isEqualTo("Organisation, Incorporated");
    assertThat(parsedMessage.sourceSystemId).isEqualTo("The Source System");
    assertThat(parsedMessage.facility.id).isEqualTo("ABC-123");
    assertThat(parsedMessage.meter.id).isEqualTo("112");
  }
}
