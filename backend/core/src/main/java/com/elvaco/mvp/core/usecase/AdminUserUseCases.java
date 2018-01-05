package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.dto.UserDto;

public class AdminUserUseCases {

  private final Users users;

  public AdminUserUseCases(Users users) {
    this.users = users;
  }

  public Optional<UserDto> findById(Long id) {
    return users.findById(id);
  }

  public List<UserDto> findAll() {
    return users.findAll();
  }

  public UserDto save(UserDto user) {
    return users.save(user);
  }

  public void deleteById(Long id) {
    users.deleteById(id);
  }
}
