package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.dto.UserDto;

public class UserUseCases {

  private final Users users;

  public UserUseCases(Users users) {
    this.users = users;
  }

  public List<UserDto> findAll() {
    return users.findAll();
  }

  public Optional<UserDto> findByEmail(String email) {
    return users.findByEmail(email);
  }

  public Optional<UserDto> findById(Long id) {
    return users.findById(id);
  }

  public UserDto save(UserDto user) {
    return users.save(user);
  }

  public void deleteById(Long id) {
    users.deleteById(id);
  }
}
