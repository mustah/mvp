package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class OrganisationDto {

  public String id;
  public String name;
  public String code;

  public OrganisationDto() {}

  public OrganisationDto(String name, String code) {
    this(null, name, code);
  }

  public OrganisationDto(String id, String name, String code) {
    this.id = id;
    this.name = name;
    this.code = code;
  }
}
