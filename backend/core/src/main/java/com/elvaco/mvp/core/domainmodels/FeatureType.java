package com.elvaco.mvp.core.domainmodels;

public enum FeatureType {

  UPDATE_GEOLOCATION("update.geolocation");

  public final String key;

  FeatureType(String key) {
    this.key = key;
  }
}
