package com.elvaco.mvp.web.security;

import java.util.Optional;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.security.TokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@RequiredArgsConstructor
public class UserDetailAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;

  @Override
  protected final UserDetails retrieveUser(
    String username,
    UsernamePasswordAuthenticationToken authentication
  ) throws AuthenticationException {
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
  ) throws AuthenticationException {
    Optional.ofNullable(authentication.getCredentials())
      .map(Object::toString)
      .filter(rawPassword -> passwordEncoder.matches(rawPassword, userDetails.getPassword()))
      .orElseThrow(this::badCredentials);
    tokenService.saveToken((AuthenticatedUser) userDetails);
  }

  private BadCredentialsException badCredentials() {
    return new BadCredentialsException(messages.getMessage(
      "AbstractUserDetailsAuthenticationProvider.badCredentials",
      "Bad credentials"
    ));
  }
}
