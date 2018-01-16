package com.elvaco.mvp.dto;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString
public class UserWithPasswordDto extends UserDto {

  @Nullable
  public String password;
}
