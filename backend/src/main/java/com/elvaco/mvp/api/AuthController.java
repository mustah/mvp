package com.elvaco.mvp.api;

import com.elvaco.mvp.core.dto.UserDto;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.exception.UserNotFound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;

@RestApi("/api/authenticate")
public class AuthController {

  private final UserUseCases userUseCases;

  @Autowired
  public AuthController(UserUseCases userUseCases) {
    this.userUseCases = userUseCases;
  }

  @RequestMapping
  public UserDto authenticate() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return userUseCases.findByEmail(email)
      .orElseThrow(() -> new UserNotFound(email));
  }
}
