package com.elvaco.mvp.web.dto;

import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
public class UserTokenDto {

  public UserDto user;
  public String token;

  public UserTokenDto(UserDto user, String token) {
    this.user = user;
    this.token = token;
  }
}
