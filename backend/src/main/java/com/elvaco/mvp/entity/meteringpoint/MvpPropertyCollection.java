package com.elvaco.mvp.entity.meteringpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class MvpPropertyCollection {
  ObjectNode json;
  public MvpPropertyCollection() {
    json = new ObjectMapper().createObjectNode();
  }
  public MvpPropertyCollection(ObjectNode json) {
    this.json = json;
  }

  public ObjectNode getJson() {
    return json;
  }

  public void put(String name, String s) {
    json.put(name, s);
  }

  public void put(String name, List<Integer> l) {

    ArrayNode arrayNode = json.putArray(name);
    for (Integer v : l ) {
      arrayNode.add(v);
    }
  }

}
