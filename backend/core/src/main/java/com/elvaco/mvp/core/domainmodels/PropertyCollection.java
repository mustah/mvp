package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class PropertyCollection {

  @Nullable
  public final UserProperty userProperty;

  public PropertyCollection(@Nullable UserProperty userProperty) {
    this.userProperty = userProperty;
  }
}
