package com.elvaco.mvp.core.dto;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class UserDto {

  public Long id;
  public String name;
  public String email;
  public OrganisationDto organisation;
  public List<String> roles;
}
