package com.elvaco.mvp.database.entity.meter;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class JsonFieldTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  public void sameInstanceEquals() {
    JsonField hello = fromString("{\"hello\": 3}");
    assertThat(hello)
      .isEqualTo(hello);
  }

  @Test
  public void otherInstanceSamePropertiesEquals() {
    JsonField firstBye = fromString("{\"bye\": 3}");
    JsonField secondBye = fromString("{\"bye\": 3}");
    assertThat(firstBye)
      .isEqualTo(secondBye);
  }

  @Test
  public void differentOrderDoesNotEqual() {
    JsonField firstNot = fromString("{\"not\": 3, \"sure\": 4}");
    JsonField firstSure = fromString("{\"sure\": 4, \"hello\": 3}");
    assertThat(firstNot)
      .isNotEqualTo(firstSure);
  }

  @Test
  public void hierarchyEquals() {
    JsonField firstPiano = fromString("{\"piano\": {\"monkey\": 4}}");
    JsonField secondPiano = fromString("{\"piano\": {\"monkey\": 4}}");
    assertThat(firstPiano)
      .isEqualTo(secondPiano);
  }

  private JsonField fromString(String s) {
    try {
      return new JsonField((ObjectNode) OBJECT_MAPPER.readTree(s));
    } catch (IOException e) {
      // Unnecessary to clutter all test methods by bubbling checked exception
      throw new RuntimeException(e);
    }
  }
}
