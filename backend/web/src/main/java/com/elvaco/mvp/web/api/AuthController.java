package com.elvaco.mvp.web.api;

import java.util.Optional;

import com.elvaco.mvp.web.dto.UserTokenDto;
import com.elvaco.mvp.web.exception.UserNotFound;
import com.elvaco.mvp.web.mapper.UserMapper;
import com.elvaco.mvp.web.security.MvpUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

@RestApi("/v1/api/authenticate")
public class AuthController {

  private final UserMapper userMapper;

  @Autowired
  public AuthController(UserMapper userMapper) {
    this.userMapper = userMapper;
  }

  @GetMapping
  public UserTokenDto authenticate(Authentication authentication) {
    String email = authentication.getName();
    return Optional.ofNullable(authentication.getPrincipal())
      .map(principal -> ((MvpUserDetails) principal))
      .map(this::toUserTokenDto)
      .orElseThrow(() -> new UserNotFound(email));
  }

  private UserTokenDto toUserTokenDto(MvpUserDetails mvpUserDetails) {
    return new UserTokenDto(userMapper.toDto(mvpUserDetails.getUser()), mvpUserDetails.getToken());
  }
}
