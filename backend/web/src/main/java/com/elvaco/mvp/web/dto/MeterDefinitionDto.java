package com.elvaco.mvp.web.dto;

import java.util.Collections;
import java.util.Set;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MeterDefinitionDto {
  @Nullable
  public Long id;

  @NotBlank
  public String name;

  @NotNull
  @Valid
  public Set<QuantityDto> quantities = Collections.emptySet();

  @Nullable
  public OrganisationDto organisation;

  @NotNull
  public IdNamedDto medium;

  public boolean autoApply;
}
