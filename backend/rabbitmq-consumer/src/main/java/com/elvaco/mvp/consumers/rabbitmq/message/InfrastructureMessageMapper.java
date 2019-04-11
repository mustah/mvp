package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.ToString;

import static java.util.Map.entry;

@ToString
public class InfrastructureMessageMapper {

  private static final Map<String, String> MAPPINGS = Map.ofEntries(
    entry("MF", "Message format"),
    entry("UT", "Uptime"),
    entry("TS", "Timestamp"),
    entry("APN", "APN"),
    entry("CID", "Network Cell Id"),
    entry("RSSI", "RSSI"),
    entry("SNR", "SNR"),
    entry("ECL", "ECL"),
    entry("NC", "Network Class"),
    entry("ACC", "Current 24h"),
    entry("BM", "Battery Monitor")
  );

  public static ObjectNode convert(JsonNode input) {
    ObjectNode output = JsonNodeFactory.instance.objectNode();

    input.fields().forEachRemaining(entry -> {
      output.set(MAPPINGS.getOrDefault(entry.getKey(), entry.getKey()), entry.getValue());
    });

    output.remove("EUI");

    return output;
  }
}
