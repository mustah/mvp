package com.elvaco.mvp.api;

import java.util.List;

import com.elvaco.mvp.core.dto.UserDto;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.exception.UserNotFound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RestApi("/api/users")
public class UserController {

  private final UserUseCases userUseCases;

  @Autowired
  UserController(UserUseCases userUseCases) {
    this.userUseCases = userUseCases;
  }

  @RequestMapping("{id}")
  public UserDto userById(@PathVariable Long id) {
    return userUseCases.findById(id)
      .orElseThrow(() -> new UserNotFound(String.valueOf(id)));
  }

  @RequestMapping
  public List<UserDto> allUsers() {
    return userUseCases.findAll();
  }
}
