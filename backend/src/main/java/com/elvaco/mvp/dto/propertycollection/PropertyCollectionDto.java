package com.elvaco.mvp.dto.propertycollection;

public class PropertyCollectionDto {

  public UserPropertyDto user;
  public SystemPropertyDto system;

  public PropertyCollectionDto() {
  }

  public PropertyCollectionDto(UserPropertyDto user) {
    this.user = user;
  }
}
