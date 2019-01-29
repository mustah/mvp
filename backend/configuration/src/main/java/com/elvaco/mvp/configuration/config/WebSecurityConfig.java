package com.elvaco.mvp.configuration.config;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.web.security.TokenAuthenticationFilter;
import com.elvaco.mvp.web.security.TokenAuthenticationProvider;
import com.elvaco.mvp.web.security.UserDetailAuthenticationProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import static com.elvaco.mvp.web.util.Constants.API_V1;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private static final String API = API_V1 + "/**";

  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;

  @Override
  public UserDetailsService userDetailsService() {
    return userDetailsService;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    auth
      .authenticationProvider(
        new UserDetailAuthenticationProvider(
          userDetailsService,
          passwordEncoder,
          tokenService
        )
      )
      .authenticationProvider(new TokenAuthenticationProvider());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
      .antMatchers(HttpMethod.OPTIONS, API).permitAll()
      .antMatchers(API_V1 + "/geocodes/**").permitAll()
      .antMatchers(API_V1 + "/logout").permitAll();

    http.headers()
      .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN)
      .and()
      .frameOptions().deny()
      .contentSecurityPolicy("default-src 'self'");

    http.csrf().disable();

    http
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests().antMatchers(API).fullyAuthenticated()
      .and()
      .httpBasic().authenticationEntryPoint(new RestAuthenticationEntryPoint())
      .and()
      .addFilterAfter(tokenAuthenticationFilter(), BasicAuthenticationFilter.class);
  }

  private TokenAuthenticationFilter tokenAuthenticationFilter() {
    return new TokenAuthenticationFilter(tokenService);
  }

  private static class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String FORM_BASED = "FormBased";

    @Override
    public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException
    ) throws IOException {
      response.setHeader(WWW_AUTHENTICATE, FORM_BASED);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
  }
}
