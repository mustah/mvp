package com.elvaco.mvp.dto.propertycollection;

public class PropertyCollectionDTO {

  public UserPropertyDTO user;
  public SystemPropertyDTO system;

  public PropertyCollectionDTO() {}

  public PropertyCollectionDTO(UserPropertyDTO user) {
    this.user = user;
  }
}
