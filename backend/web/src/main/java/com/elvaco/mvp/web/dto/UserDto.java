package com.elvaco.mvp.web.dto;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Language;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class UserDto {

  public UUID id;
  public String name;
  public String email;
  public Language language;
  public OrganisationDto organisation;
  public List<String> roles;

  public UserDto(
    UUID id,
    String name,
    String email,
    Language language,
    OrganisationDto organisation,
    List<String> roles
  ) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.language = language;
    this.organisation = organisation;
    this.roles = roles;
  }
}
