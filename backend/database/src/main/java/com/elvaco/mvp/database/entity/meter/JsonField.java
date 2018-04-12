package com.elvaco.mvp.database.entity.meter;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.ToString;

import static com.elvaco.mvp.database.util.Json.OBJECT_MAPPER;
import static com.elvaco.mvp.database.util.Json.toJsonNode;
import static com.elvaco.mvp.database.util.Json.toObject;

@ToString
public class JsonField implements Serializable {

  private static final long serialVersionUID = -7594482554930519496L;
  private transient ObjectNode json;

  public JsonField(ObjectNode json) {
    this.json = json.deepCopy();
  }

  public JsonField() {
    this(OBJECT_MAPPER.createObjectNode());
  }

  public ObjectNode getJson() {
    return json.deepCopy();
  }

  public JsonField put(String fieldName, Object value) {
    ObjectNode newObjectNode = json.deepCopy();
    newObjectNode.set(fieldName, toJsonNode(value));
    return new JsonField(newObjectNode);
  }

  public JsonField putArray(String fieldName, List<Integer> l) {
    ObjectNode newObjectNode = json.deepCopy();
    ArrayNode arrayNode = newObjectNode.putArray(fieldName);
    for (Integer v : l) {
      arrayNode.add(v);
    }
    return new JsonField(newObjectNode);
  }

  public String asJsonString() {
    return json.toString();
  }

  public Optional<JsonNode> get(String fieldName) {
    return Optional.ofNullable(json.deepCopy().get(fieldName));
  }

  public <T> Optional<T> asObject(String fieldName, Class<T> valueType) {
    T returnObject = null;
    if (json.has(fieldName)) {
      returnObject = toObject(json.deepCopy().get(fieldName).toString(), valueType);
    }
    return Optional.ofNullable(returnObject);
  }

  public Optional<Double> getDoubleValue(String fieldName) {
    return get(fieldName).map(JsonNode::doubleValue);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof JsonField)) {
      return false;
    }

    JsonField other = (JsonField) o;

    return json.equals(other.json);
  }

  @Override
  public int hashCode() {
    return json.hashCode();
  }
}
