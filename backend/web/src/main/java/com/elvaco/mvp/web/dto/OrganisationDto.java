package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class OrganisationDto {

  public String id;
  public String name;
  public String slug;

  public OrganisationDto() {}

  public OrganisationDto(String name, String slug) {
    this(null, name, slug);
  }

  public OrganisationDto(String id, String name, String slug) {
    this.id = id;
    this.name = name;
    this.slug = slug;
  }
}
