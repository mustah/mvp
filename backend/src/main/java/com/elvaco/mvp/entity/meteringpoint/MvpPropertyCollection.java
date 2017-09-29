package com.elvaco.mvp.entity.meteringpoint;

import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.ToString;

import static com.elvaco.mvp.utils.Json.OBJECT_MAPPER;
import static com.elvaco.mvp.utils.Json.toJsonNode;

@ToString
public class MvpPropertyCollection {

  private final ObjectNode json;

  public MvpPropertyCollection(ObjectNode json) {
    this.json = json;
  }

  public MvpPropertyCollection() {
    this(OBJECT_MAPPER.createObjectNode());
  }

  public ObjectNode getJson() {
    return json;
  }

  public MvpPropertyCollection put(String fieldName, Object value) {
    json.set(fieldName, toJsonNode(value));
    return this;
  }

  public MvpPropertyCollection putArray(String fieldName, List<Integer> l) {
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
