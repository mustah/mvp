package com.elvaco.mvp.api;

import java.util.List;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.dto.UserDto;
import com.elvaco.mvp.core.usecase.UserUseCases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestApi("/api/users")
public class UserController {

  private final UserUseCases userUseCases;

  @Autowired
  UserController(UserUseCases userUseCases) {
    this.userUseCases = userUseCases;
  }

  @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
  @GetMapping
  public List<UserDto> allUsers() {
    return userUseCases.findAll();
  }

  @Nullable
  @GetMapping("{id}")
  public UserDto userById(@PathVariable Long id) {
    return userUseCases.findById(id).orElse(null);
  }

  @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
  @PostMapping
  public UserDto createUser(@RequestBody UserDto user) {
    return userUseCases.save(user);
  }

  @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
  @PutMapping
  public UserDto updateUser(@RequestBody UserDto user) {
    return userUseCases.save(user);
  }

  @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
  @DeleteMapping("{id}")
  public void deleteUser(@PathVariable Long id) {
    userUseCases.deleteById(id);
  }
}
