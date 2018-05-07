package com.elvaco.geoservice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class FieldErrorsDto {
  public int status;
  public List<String> fieldErrors;
}
