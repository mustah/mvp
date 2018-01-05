package com.elvaco.mvp.core.dto;

import java.util.List;

import lombok.ToString;

@ToString
public class UserDto {

  public Long id;
  public String name;
  public String email;
  public OrganisationDto company;
  public List<String> roles;
}
