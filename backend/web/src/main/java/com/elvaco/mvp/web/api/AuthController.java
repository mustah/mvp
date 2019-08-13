package com.elvaco.mvp.web.api;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.web.dto.UserTokenDto;
import com.elvaco.mvp.web.exception.UserNotFound;
import com.elvaco.mvp.web.mapper.UserTokenDtoMapper;
import com.elvaco.mvp.web.security.MvpUserDetails;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;

import static com.elvaco.mvp.web.util.RequestHelper.bearerTokenFrom;

@AllArgsConstructor
@RestApi
public class AuthController {

  private final TokenService tokenService;

  @GetMapping("/authenticate")
  public UserTokenDto authenticate(Authentication authentication) {
    String email = authentication.getName();
    return Optional.ofNullable(authentication.getPrincipal())
      .map(principal -> ((MvpUserDetails) principal))
      .map(UserTokenDtoMapper::toUserTokenDto)
      .orElseThrow(() -> UserNotFound.withUsername(email));
  }

  @GetMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    bearerTokenFrom(request).ifPresent(tokenService::removeToken);
    SecurityContextHolder.clearContext();
    return ResponseEntity.noContent().build();
  }
}
