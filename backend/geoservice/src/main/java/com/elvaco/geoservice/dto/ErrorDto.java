package com.elvaco.geoservice.dto;

public class ErrorDto {
  private Integer errorCode;
  private String message;
  private AddressDto address;

  public String getMessage() {
    return message;
  }

  public ErrorDto setMessage(String message) {
    this.message = message;
    return this;
  }

  public Integer getErrorCode() {
    return errorCode;
  }

  public ErrorDto setErrorCode(Integer errorCode) {
    this.errorCode = errorCode;
    return this;
  }

  public AddressDto getAddress() {
    return address;
  }

  public void setAddress(AddressDto address) {
    this.address = address;
  }

}
