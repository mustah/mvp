package com.elvaco.mvp.core.domainmodels;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class UserProperty {

  public final String externalId;
  public final String project;

  public UserProperty(String externalId, String project) {
    this.externalId = externalId;
    this.project = project;
  }
}
