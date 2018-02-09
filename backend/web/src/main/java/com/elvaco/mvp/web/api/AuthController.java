package com.elvaco.mvp.web.api;

import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.web.dto.UserDto;
import com.elvaco.mvp.web.exception.UserNotFound;
import com.elvaco.mvp.web.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

@RestApi("/v1/api/authenticate")
public class AuthController {

  private final UserUseCases userUseCases;
  private final UserMapper userMapper;

  @Autowired
  public AuthController(UserUseCases userUseCases, UserMapper userMapper) {
    this.userUseCases = userUseCases;
    this.userMapper = userMapper;
  }

  @GetMapping
  public UserDto authenticate(Authentication authentication) {
    String email = authentication.getName();
    return userUseCases.findByEmail(email)
      .map(userMapper::toDto)
      .orElseThrow(() -> new UserNotFound(email));
  }
}
