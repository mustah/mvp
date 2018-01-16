package com.elvaco.mvp.api;

import java.util.List;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.dto.UserDto;
import com.elvaco.mvp.dto.UserWithPasswordDto;
import com.elvaco.mvp.exception.UserNotFound;
import com.elvaco.mvp.mapper.UserDetailsMapper;
import com.elvaco.mvp.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static java.util.stream.Collectors.toList;

@RestApi("/api/users")
public class UserController {

  private final UserUseCases userUseCases;
  private final UserMapper userMapper;
  private final UserDetailsManager userDetailsService;

  @Autowired
  UserController(
    UserUseCases userUseCases,
    UserMapper userMapper,
    UserDetailsManager userDetailsService
  ) {
    this.userUseCases = userUseCases;
    this.userMapper = userMapper;
    this.userDetailsService = userDetailsService;
  }

  @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
  @GetMapping
  public List<UserDto> allUsers() {
    return userUseCases.findAll()
      .stream()
      .map(userMapper::toDto)
      .collect(toList());
  }

  @Nullable
  @GetMapping("{id}")
  public UserDto userById(@PathVariable Long id) {
    return userUseCases.findById(id)
      .map(userMapper::toDto)
      .orElse(null);
  }

  @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
  @PostMapping
  public UserDto createUser(@RequestBody UserWithPasswordDto user) {
    User newUser = userUseCases.create(userMapper.toDomainModel(user));
    userDetailsService.createUser(UserDetailsMapper.toUserDetails(newUser));
    return userMapper.toDto(newUser);
  }

  @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
  @PutMapping
  public UserDto updateUser(@RequestBody UserDto user) {
    return userUseCases.update(userMapper.toDomainModel(user))
      .map(userMapper::toDto)
      .orElseThrow(() -> new UserNotFound(user.id));
  }

  @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
  @DeleteMapping("{id}")
  public UserDto deleteUser(@PathVariable Long id) {
    UserDto user = userUseCases.findById(id)
      .map(userMapper::toDto)
      .orElseThrow(() -> new UserNotFound(id));
    // TODO[!must!] delete should actually not remove the entity, just mark it as deleted.
    userUseCases.deleteById(id);
    return user;
  }
}
