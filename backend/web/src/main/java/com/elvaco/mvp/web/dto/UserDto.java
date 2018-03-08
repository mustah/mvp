package com.elvaco.mvp.web.dto;

import java.util.List;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class UserDto {

  public String id;
  public String name;
  public String email;
  public OrganisationDto organisation;
  public List<String> roles;

  public UserDto(
    UUID id,
    String name,
    String email,
    OrganisationDto organisation,
    List<String> roles
  ) {
    this.id = id.toString();
    this.name = name;
    this.email = email;
    this.organisation = organisation;
    this.roles = roles;
  }
}
