package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class OrganisationDto {

  public Long id;
  public String name;
  public String code;

  public OrganisationDto() {}

  public OrganisationDto(Long id, String name, String code) {
    this.id = id;
    this.name = name;
    this.code = code;
  }
}
