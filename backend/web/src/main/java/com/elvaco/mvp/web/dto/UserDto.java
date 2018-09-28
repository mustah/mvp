package com.elvaco.mvp.web.dto;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Language;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class UserDto {

  public UUID id;
  public String name;
  public String email;
  public Language language;
  public OrganisationDto organisation;
  public List<String> roles;
}
