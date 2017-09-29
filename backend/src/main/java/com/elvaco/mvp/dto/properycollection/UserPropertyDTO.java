package com.elvaco.mvp.dto.properycollection;

public class UserPropertyDTO {

  public String externalId;
  public String project;

  public UserPropertyDTO() {}

  public UserPropertyDTO(String externalId) {
    this(externalId, null);
  }

  public UserPropertyDTO(String externalId, String project) {
    this.externalId = externalId;
    this.project = project;
  }
}
