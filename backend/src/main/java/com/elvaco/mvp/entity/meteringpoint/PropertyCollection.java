package com.elvaco.mvp.entity.meteringpoint;

import static com.elvaco.mvp.utils.Json.OBJECT_MAPPER;
import static com.elvaco.mvp.utils.Json.toJsonNode;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import lombok.ToString;

@ToString
public class PropertyCollection {

  private final ObjectNode json;

  public PropertyCollection(ObjectNode json) {
    this.json = json;
  }

  public PropertyCollection() {
    this(OBJECT_MAPPER.createObjectNode());
  }

  public ObjectNode getJson() {
    return json;
  }

  public PropertyCollection put(String fieldName, Object value) {
    json.set(fieldName, toJsonNode(value));
    return this;
  }

  public PropertyCollection putArray(String fieldName, List<Integer> l) {
    ArrayNode arrayNode = json.putArray(fieldName);
    for (Integer v : l) {
      arrayNode.add(v);
    }
    return this;
  }

  public String asJsonString() {
    return json.toString();
  }
}
