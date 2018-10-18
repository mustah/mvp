package com.elvaco.mvp.web.dto;

import java.util.UUID;
import javax.annotation.Nullable;

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

  @Nullable
  public OrganisationDto parent;

  public OrganisationDto(String name, String slug) {
    this(null, name, slug);
  }

  public OrganisationDto(UUID id, String name, String slug) {
    this(id, name, slug, null);
  }

}
