package com.elvaco.mvp.dto;

public class ErrorMessageDTO {

  public String message;
  public int status;

  public ErrorMessageDTO() {}

  public ErrorMessageDTO(String message, int status) {
    this.message = message;
    this.status = status;
  }
}
