package com.elvaco.mvp.web.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class OrganisationDto {

  public UUID id;
  public String name;
  public String slug;

  public OrganisationDto(String name, String slug) {
    this(null, name, slug);
  }
}
