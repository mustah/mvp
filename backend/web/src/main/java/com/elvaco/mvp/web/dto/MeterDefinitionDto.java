package com.elvaco.mvp.web.dto;

import java.util.Set;
import javax.annotation.Nullable;

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
  public String name;
  public Set<QuantityDto> quantities;
  @Nullable
  public OrganisationDto organisation;
  public IdNamedDto medium;
  public boolean autoApply;
}
