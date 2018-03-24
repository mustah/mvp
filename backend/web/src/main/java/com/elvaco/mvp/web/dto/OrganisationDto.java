package com.elvaco.mvp.web.dto;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class OrganisationDto {

  public UUID id;
  public String name;
  public String slug;

  public OrganisationDto(String name, String slug) {
    this(null, name, slug);
  }

  public OrganisationDto(UUID id, String name, String slug) {
    this.id = id;
    this.name = name;
    this.slug = slug;
  }
}
