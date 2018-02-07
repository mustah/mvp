package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class UserProperty {

  @Nullable
  public String externalId;
  @Nullable
  public String project;

  public UserProperty() {}

  public UserProperty(@Nullable String externalId, @Nullable String project) {
    this.externalId = externalId;
    this.project = project;
  }

  public UserProperty(@Nullable String externalId) {
    this(externalId, null);
  }
}
