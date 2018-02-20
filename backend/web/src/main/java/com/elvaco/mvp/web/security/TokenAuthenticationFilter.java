package com.elvaco.mvp.web.security;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.web.exception.InvalidToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import static com.elvaco.mvp.web.util.Constants.AUTHORIZATION;
import static com.elvaco.mvp.web.util.Constants.BEARER;

@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

  private final TokenService tokenService;

  public TokenAuthenticationFilter(TokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain chain
  )
    throws IOException, ServletException {
    String header = request.getHeader(AUTHORIZATION);
    if (header == null || !header.startsWith(BEARER)) {
      chain.doFilter(request, response);
      return;
    }

    try {
      setAuthentication(getAuthenticationTokenFrom(header));
      chain.doFilter(request, response);
    } catch (AuthenticationException e) {
      SecurityContextHolder.clearContext();
      response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
    }
  }

  private void setAuthentication(Authentication authentication) {
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private Authentication getAuthenticationTokenFrom(String requestHeader) {
    return Optional.ofNullable(requestHeader)
      .map(header -> header.replace(BEARER, ""))
      .flatMap(tokenService::getToken)
      .map(AuthenticatedUser::getToken)
      .map(AuthenticationToken::new)
      .orElseThrow(InvalidToken::new);
  }
}
