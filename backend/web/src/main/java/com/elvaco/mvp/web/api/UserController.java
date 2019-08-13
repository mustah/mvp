package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.security.TokenFactory;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.web.dto.PasswordDto;
import com.elvaco.mvp.web.dto.UserDto;
import com.elvaco.mvp.web.dto.UserTokenDto;
import com.elvaco.mvp.web.dto.UserWithPasswordDto;
import com.elvaco.mvp.web.exception.InvalidParameter;
import com.elvaco.mvp.web.exception.UserNotFound;
import com.elvaco.mvp.web.mapper.UserDtoMapper;
import com.elvaco.mvp.web.mapper.UserTokenDtoMapper;
import com.elvaco.mvp.web.security.AuthenticationToken;
import com.elvaco.mvp.web.security.MvpUserDetails;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.elvaco.mvp.web.mapper.UserDtoMapper.toDomainModel;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor
@RestApi("/api/v1/users")
public class UserController {

  private final UserUseCases userUseCases;
  private final TokenFactory tokenFactory;
  private final TokenService tokenService;

  @GetMapping
  public List<UserDto> allUsers() {
    return userUseCases.findAll().stream()
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

  @PutMapping("/change-password/{userId}")
  public UserTokenDto changePassword(@PathVariable UUID userId, @RequestBody PasswordDto password) {
    if (password.password == null || password.password.trim().isEmpty()) {
      throw new InvalidParameter("password");
    }

    return userUseCases.changePassword(userId, password.password)
      .map(this::getMvpUserDetails)
      .map(UserTokenDtoMapper::toUserTokenDto)
      .orElseThrow(() -> UserNotFound.withId(userId));
  }

  @PutMapping
  public UserDto updateUser(@RequestBody UserWithPasswordDto user) {
    return userUseCases.update(toDomainModel(user))
      .map(UserDtoMapper::toDto)
      .orElseThrow(() -> UserNotFound.withId(user.id));
  }

  @DeleteMapping("{id}")
  public UserDto deleteUser(@PathVariable UUID id) {
    return userUseCases.delete(id)
      .map(UserDtoMapper::toDto)
      .orElseThrow(() -> UserNotFound.withId(id));
  }

  private MvpUserDetails getMvpUserDetails(User user) {
    MvpUserDetails mvpUserDetails = (MvpUserDetails) SecurityContextHolder.getContext()
      .getAuthentication()
      .getPrincipal();
    if (mvpUserDetails.getUserId() == user.id) {
      AuthenticatedUser authenticatedUser = new MvpUserDetails(user, tokenFactory.newToken());
      tokenService.saveToken(authenticatedUser);
      SecurityContextHolder.getContext()
        .setAuthentication(AuthenticationToken.from(authenticatedUser));

      return new MvpUserDetails(user, authenticatedUser.getToken());
    } else {
      return mvpUserDetails;
    }
  }
}
