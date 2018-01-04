package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.dto.UserDto;

public interface Users {

  Optional<UserDto> findByEmail(String email);

  Optional<UserDto> findById(Long id);

  List<UserDto> findAll();
}
