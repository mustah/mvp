package com.elvaco.mvp.dto;

public class ErrorMessageDto {

  public String message;
  public int status;

  public ErrorMessageDto() {}

  public ErrorMessageDto(String message, int status) {
    this.message = message;
    this.status = status;
  }
}
