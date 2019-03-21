package com.elvaco.mvp.web.dto;

public enum WidgetType {

  COLLECTION("collection"),
  MAP("map");

  public final String name;

  WidgetType(String name) {
    this.name = name;
  }
}
