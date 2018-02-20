package com.elvaco.mvp.web.dto;

import lombok.ToString;

@ToString
public class UserTokenDto {

  public UserDto user;
  public String token;

  public UserTokenDto() {}

  public UserTokenDto(UserDto user, String token) {
    this.user = user;
    this.token = token;
  }
}
