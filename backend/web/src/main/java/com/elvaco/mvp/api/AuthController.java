package com.elvaco.mvp.api;

import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.dto.UserDto;
import com.elvaco.mvp.exception.UserNotFound;
import com.elvaco.mvp.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;

@RestApi("/v1/api/authenticate")
public class AuthController {

  private final UserUseCases userUseCases;
  private final UserMapper userMapper;

  @Autowired
  public AuthController(UserUseCases userUseCases, UserMapper userMapper) {
    this.userUseCases = userUseCases;
    this.userMapper = userMapper;
  }

  @RequestMapping
  public UserDto authenticate() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return userUseCases.findByEmail(email)
      .map(userMapper::toDto)
      .orElseThrow(() -> new UserNotFound(email));
  }
}
