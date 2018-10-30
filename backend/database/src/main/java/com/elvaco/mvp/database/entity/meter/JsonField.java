package com.elvaco.mvp.database.entity.meter;

import java.io.Serializable;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.ToString;

import static com.elvaco.mvp.core.util.Json.OBJECT_MAPPER;
import static com.elvaco.mvp.core.util.Json.toJsonNode;

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

  public String asJsonString() {
    return json.toString();
  }

  public Optional<JsonNode> get(String fieldName) {
    return Optional.ofNullable(json.deepCopy().get(fieldName));
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
