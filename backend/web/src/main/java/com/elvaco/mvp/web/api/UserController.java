package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.web.dto.UserDto;
import com.elvaco.mvp.web.dto.UserWithPasswordDto;
import com.elvaco.mvp.web.exception.UserNotFound;
import com.elvaco.mvp.web.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static java.util.stream.Collectors.toList;

@RestApi("/v1/api/users")
public class UserController {

  private final UserUseCases userUseCases;
  private final UserMapper userMapper;

  @Autowired
  UserController(UserUseCases userUseCases, UserMapper userMapper) {
    this.userUseCases = userUseCases;
    this.userMapper = userMapper;
  }

  @GetMapping
  public List<UserDto> allUsers() {
    return userUseCases.findAll()
      .stream()
      .map(userMapper::toDto)
      .collect(toList());
  }

  @GetMapping("{id}")
  public UserDto userById(@PathVariable String id) {
    return userUseCases.findById(UUID.fromString(id))
      .map(userMapper::toDto)
      .orElseThrow(() -> UserNotFound.withId(id));
  }

  @PostMapping
  public ResponseEntity<UserDto> createUser(@RequestBody UserWithPasswordDto user) {
    Optional<UserDto> createdUser = userUseCases.create(
      userMapper.toDomainModel(user)
    ).map(userMapper::toDto);

    return createdUser.map(
      userDto ->
        ResponseEntity.status(HttpStatus.CREATED).body(userDto))
      .orElse(
        ResponseEntity.status(HttpStatus.FORBIDDEN).build()
      );
  }

  @PutMapping
  public UserDto updateUser(@RequestBody UserDto user) {
    return userUseCases.update(userMapper.toDomainModel(user))
      .map(userMapper::toDto)
      .orElseThrow(() -> UserNotFound.withId(user.id));
  }

  @DeleteMapping("{id}")
  public UserDto deleteUser(@PathVariable String id) {
    User user = userUseCases.findById(UUID.fromString(id))
      .orElseThrow(() -> UserNotFound.withId(id));
    // TODO[!must!] delete should actually not remove the entity, just mark it as deleted.
    userUseCases.delete(user);
    return userMapper.toDto(user);
  }
}
