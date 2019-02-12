package com.elvaco.mvp.web.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class QuantityDto {
  @NotBlank
  public String name;
  public boolean consumption;
  @NotBlank
  public String displayUnit;
  @PositiveOrZero
  public int precision;
}
