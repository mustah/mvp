package com.elvaco.mvp.web.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ErrorMessageDto {

  public String message;
  public int status;
}
