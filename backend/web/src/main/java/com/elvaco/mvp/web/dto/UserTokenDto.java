package com.elvaco.mvp.web.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserTokenDto {

  public UserDto user;
  public String token;
}
