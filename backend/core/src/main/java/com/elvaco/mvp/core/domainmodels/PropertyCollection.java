package com.elvaco.mvp.core.domainmodels;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class PropertyCollection {
  public final UserProperty userProperty;

  public PropertyCollection(UserProperty userProperty) {
    this.userProperty = userProperty;
  }
}
