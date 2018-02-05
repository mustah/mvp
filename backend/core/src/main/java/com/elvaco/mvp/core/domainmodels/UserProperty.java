package com.elvaco.mvp.core.domainmodels;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class UserProperty {

  public String externalId;
  public String project;

  public UserProperty() {}

  public UserProperty(String externalId, String project) {
    this.externalId = externalId;
    this.project = project;
  }
}
