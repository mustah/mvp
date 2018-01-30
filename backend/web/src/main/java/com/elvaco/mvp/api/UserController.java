package com.elvaco.mvp.api;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.dto.UserDto;
import com.elvaco.mvp.dto.UserWithPasswordDto;
import com.elvaco.mvp.exception.UserNotFound;
import com.elvaco.mvp.mapper.UserMapper;
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
  public UserDto userById(@PathVariable Long id) {
    return userUseCases.findById(id)
      .map(userMapper::toDto)
      .orElseThrow(() -> new UserNotFound(id));
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
      .orElseThrow(() -> new UserNotFound(user.id));
  }

  @DeleteMapping("{id}")
  public UserDto deleteUser(@PathVariable Long id) {
    User user = userUseCases.findById(id)
      .orElseThrow(() -> new UserNotFound(id));
    // TODO[!must!] delete should actually not remove the entity, just mark it as deleted.
    userUseCases.delete(user);
    return userMapper.toDto(user);
  }
}
