package com.elvaco.mvp.core.usecase;

import java.util.Optional;

import com.elvaco.mvp.core.dto.UserDto;

public class UserUseCases {

  private final Users users;

  public UserUseCases(Users users) {
    this.users = users;
  }

  public Optional<UserDto> findByEmail(String email) {
    return users.findByEmail(email);
  }
}
