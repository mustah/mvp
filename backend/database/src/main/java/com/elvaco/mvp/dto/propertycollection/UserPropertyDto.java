package com.elvaco.mvp.dto.propertycollection;

public class UserPropertyDto {

  public String externalId;
  public String project;

  public UserPropertyDto() {}

  public UserPropertyDto(String externalId) {
    this(externalId, null);
  }

  public UserPropertyDto(String externalId, String project) {
    this.externalId = externalId;
    this.project = project;
  }
}
