package com.elvaco.mvp.dto.properycollection;

public class PropertyCollectionDTO {

  public UserPropertyDTO user;
  public SystemPropertyDTO system;

  public PropertyCollectionDTO() {}

  public PropertyCollectionDTO(UserPropertyDTO user) {
    this.user = user;
  }
}
