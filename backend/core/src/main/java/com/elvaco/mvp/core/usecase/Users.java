package com.elvaco.mvp.core.usecase;

import java.util.Optional;

import com.elvaco.mvp.core.dto.UserDto;

public interface Users {

  Optional<UserDto> findByEmail(String email);
}
