package com.elvaco.mvp.entity.meteringpoint;

import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.elvaco.mvp.utils.Json.OBJECT_MAPPER;

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

  public void put(String name, String s) {
    json.put(name, s);
  }

  public void put(String name, List<Integer> l) {
    ArrayNode arrayNode = json.putArray(name);
    for (Integer v : l) {
      arrayNode.add(v);
    }
  }

  @Override
  public String toString() {
    return "MvpPropertyCollection{" +
           "json=" + json +
           '}';
  }
}
