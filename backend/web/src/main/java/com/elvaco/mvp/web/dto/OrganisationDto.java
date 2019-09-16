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

import static com.elvaco.mvp.core.util.Slugify.slugify;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class OrganisationDto {

  @NotNull
  public UUID id;

  @NotBlank
  public String name;

  @Nullable
  public String slug;

  @Nullable
  public String shortPrefix;

  @Nullable
  @Valid
  public OrganisationDto parent;

  @Nullable
  public UUID selectionId;

  public OrganisationDto(String name) {
    this(null, name);
  }

  public OrganisationDto(UUID id, String name) {
    this(id, name, slugify(name), null, null, null);
  }
}
