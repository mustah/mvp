package com.elvaco.mvp.web.security;

import java.util.Optional;

import com.elvaco.mvp.core.spi.security.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static java.util.Objects.requireNonNull;

@Slf4j
public class UserDetailAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;
  private final UserCache userCache;
  private final TokenService tokenService;

  public UserDetailAuthenticationProvider(
    UserDetailsService userDetailsService,
    PasswordEncoder passwordEncoder,
    UserCache userCache,
    TokenService tokenService
  ) {
    this.userDetailsService = requireNonNull(userDetailsService);
    this.passwordEncoder = requireNonNull(passwordEncoder);
    this.userCache = requireNonNull(userCache);
    this.tokenService = tokenService;
    setUserCache(userCache);
  }

  @Override
  protected final UserDetails retrieveUser(
    String username,
    UsernamePasswordAuthenticationToken authentication
  )
    throws AuthenticationException {
    try {
      return userDetailsService.loadUserByUsername(username);
    } catch (UsernameNotFoundException userNotFound) {
      throw userNotFound;
    } catch (Exception ex) {
      throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
    }
  }

  @Override
  protected void additionalAuthenticationChecks(
    UserDetails userDetails,
    UsernamePasswordAuthenticationToken authentication
  )
    throws AuthenticationException {
    if (isNotAuthenticated(userDetails)) {
      log.info("User '{}' is not in cache.", userDetails.getUsername());
      Optional.ofNullable(authentication.getCredentials())
        .map(Object::toString)
        .filter(rawPassword -> passwordEncoder.matches(rawPassword, userDetails.getPassword()))
        .orElseThrow(this::badCredentials);
      MvpUserDetails authenticatedUser = (MvpUserDetails) userDetails;
      tokenService.saveToken(authenticatedUser.getToken(), authenticatedUser);
    }
  }

  private boolean isNotAuthenticated(UserDetails userDetails) {
    return userCache.getUserFromCache(userDetails.getUsername()) == null;
  }

  private BadCredentialsException badCredentials() {
    return new BadCredentialsException(messages.getMessage(
      "AbstractUserDetailsAuthenticationProvider.badCredentials",
      "Bad credentials"
    ));
  }
}
