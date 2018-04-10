package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MeteringMessageParserTest {

  private MeteringMessageParser meteringMessageParser;

  @Before
  public void setUp() {
    meteringMessageParser = new MeteringMessageParser();
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
    MeteringMeasurementMessageDto parsedMessage = meteringMessageParser
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
