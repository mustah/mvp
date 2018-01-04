package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.dto.UserDto;

public interface Users {

  List<UserDto> findAll();

  Optional<UserDto> findByEmail(String email);

  Optional<UserDto> findById(Long id);

  UserDto save(UserDto user);

  void deleteById(Long id);
}
