package com.elvaco.mvp.database.entity.meter;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.elvaco.mvp.database.util.Json.OBJECT_MAPPER;
import static com.elvaco.mvp.database.util.Json.toJsonNode;
import static com.elvaco.mvp.database.util.Json.toObject;

@ToString
@EqualsAndHashCode
public class PropertyCollection implements Serializable {

  private static final long serialVersionUID = -7594482554930519496L;
  private transient ObjectNode json;

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

  public Optional<JsonNode> get(String fieldName) {
    return Optional.ofNullable(json.get(fieldName));
  }

  public <T> Optional<T> asObject(String fieldName, Class<T> valueType) {
    T returnObject = null;
    if (json.has(fieldName)) {
      returnObject = toObject(json.get(fieldName).toString(), valueType);
    }
    return Optional.ofNullable(returnObject);
  }

  public Optional<Double> getDoubleValue(String fieldName) {
    return get(fieldName).map(JsonNode::doubleValue);
  }
}
