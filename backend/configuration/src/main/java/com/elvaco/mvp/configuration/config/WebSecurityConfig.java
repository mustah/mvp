package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.web.security.TokenAuthenticationFilter;
import com.elvaco.mvp.web.security.TokenAuthenticationProvider;
import com.elvaco.mvp.web.security.UserDetailAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import static com.elvaco.mvp.web.util.Constants.API_V1;

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private static final String API = API_V1 + "/**";
  private static final String H2_CONSOLE = "/h2-console/**";

  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;
  private final UserCache userCache;
  private final TokenService tokenService;

  @Autowired
  WebSecurityConfig(
    UserDetailsService userDetailsService,
    PasswordEncoder passwordEncoder,
    UserCache userCache,
    TokenService tokenService
  ) {
    this.userDetailsService = userDetailsService;
    this.passwordEncoder = passwordEncoder;
    this.userCache = userCache;
    this.tokenService = tokenService;
  }

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
          userCache,
          tokenService
        )
      )
      .authenticationProvider(new TokenAuthenticationProvider());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
      .antMatchers(HttpMethod.OPTIONS, API).permitAll()
      .antMatchers(API_V1 + "/logout").permitAll()
      .antMatchers(H2_CONSOLE).permitAll();
    http.csrf().disable();
    http.headers().frameOptions().disable();

    http
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests().antMatchers(API).fullyAuthenticated()
      .and()
      .httpBasic()
      .and()
      .addFilterAfter(tokenAuthenticationFilter(), BasicAuthenticationFilter.class);
  }

  private TokenAuthenticationFilter tokenAuthenticationFilter() {
    return new TokenAuthenticationFilter(tokenService);
  }
}
