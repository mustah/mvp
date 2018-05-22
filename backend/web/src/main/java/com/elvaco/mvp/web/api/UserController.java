package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.web.dto.UserDto;
import com.elvaco.mvp.web.dto.UserWithPasswordDto;
import com.elvaco.mvp.web.exception.UserNotFound;
import com.elvaco.mvp.web.mapper.UserDtoMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.elvaco.mvp.web.mapper.UserDtoMapper.toDomainModel;
import static com.elvaco.mvp.web.mapper.UserDtoMapper.toDto;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor
@RestApi("/api/v1/users")
public class UserController {

  private final UserUseCases userUseCases;

  @GetMapping
  public List<UserDto> allUsers() {
    return userUseCases.findAll()
      .stream()
      .map(UserDtoMapper::toDto)
      .collect(toList());
  }

  @GetMapping("{id}")
  public UserDto userById(@PathVariable UUID id) {
    return userUseCases.findById(id)
      .map(UserDtoMapper::toDto)
      .orElseThrow(() -> UserNotFound.withId(id));
  }

  @PostMapping
  public ResponseEntity<UserDto> createUser(@RequestBody UserWithPasswordDto user) {
    return userUseCases.create(toDomainModel(user))
      .map(UserDtoMapper::toDto)
      .map(userDto -> ResponseEntity.status(HttpStatus.CREATED).body(userDto))
      .orElseGet(ResponseEntity.status(HttpStatus.FORBIDDEN)::build);
  }

  @PutMapping
  public UserDto updateUser(@RequestBody UserDto user) {
    return userUseCases.update(toDomainModel(user))
      .map(UserDtoMapper::toDto)
      .orElseThrow(() -> UserNotFound.withId(user.id));
  }

  @DeleteMapping("{id}")
  public UserDto deleteUser(@PathVariable UUID id) {
    User user = userUseCases.findById(id)
      .orElseThrow(() -> UserNotFound.withId(id));
    userUseCases.delete(user);
    return toDto(user);
  }
}
