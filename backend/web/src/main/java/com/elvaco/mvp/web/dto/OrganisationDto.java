package com.elvaco.mvp.web.dto;

import java.util.UUID;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class OrganisationDto {

  @NotNull
  public UUID id;

  @NotBlank
  public String name;

  @NotBlank
  public String slug;

  @Nullable
  @Valid
  public OrganisationDto parent;

  @Nullable
  public UUID selectionId;

  public OrganisationDto(String name, String slug) {
    this(null, name, slug);
  }

  public OrganisationDto(UUID id, String name, String slug) {
    this(id, name, slug, null, null);
  }
}
