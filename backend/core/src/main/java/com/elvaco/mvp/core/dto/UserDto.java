package com.elvaco.mvp.core.dto;

import lombok.ToString;

@ToString
public class UserDto {

  public Long id;
  public String name;
  public String email;
  public OrganisationDto company;
}
