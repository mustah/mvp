package com.elvaco.mvp.core.exception;

import java.util.Arrays;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.AssetType;

import static java.util.stream.Collectors.toList;

public class InvalidFormat extends RuntimeException {

  private static final long serialVersionUID = 2981700686816129657L;

  private InvalidFormat(String message) {
    super(message);
  }

  public static InvalidFormat image(Set<String> allowed, String actual) {
    return new InvalidFormat(
      "You used " + actual + " but the image needs to be one of " + String.join(", ", allowed)
    );
  }

  public static InvalidFormat assetType() {
    return new InvalidFormat(
      "Invalid asset type, it needs to be one of "
        + String.join(
        ", ",
        Arrays.stream(AssetType.values()).map(AssetType::toString).collect(toList())
      )
    );
  }
}
