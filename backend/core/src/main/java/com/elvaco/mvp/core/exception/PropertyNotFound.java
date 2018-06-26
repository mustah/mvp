package com.elvaco.mvp.core.exception;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.FeatureType;

public class PropertyNotFound extends RuntimeException {

  private static final long serialVersionUID = -8504858297588980871L;

  public PropertyNotFound(FeatureType feature, UUID id) {
    super(String.format("Property '%s' for entity '%s' was not found", feature, id));
  }
}
