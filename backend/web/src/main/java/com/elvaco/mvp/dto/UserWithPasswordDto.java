package com.elvaco.mvp.dto;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString
public class UserWithPasswordDto extends UserDto {

  public String password;
}
