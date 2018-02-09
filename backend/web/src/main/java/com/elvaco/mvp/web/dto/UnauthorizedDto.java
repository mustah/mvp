package com.elvaco.mvp.web.dto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class UnauthorizedDto {

  public Long timestamp;
  public Integer status;
  public String error;
  public String message;
  public String path;
}
