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
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
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

import static com.elvaco.mvp.core.domainmodels.Role.MVP_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.MVP_USER;
import static com.elvaco.mvp.core.domainmodels.Role.OTC_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;

@RequiredArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private static final String POLICY_DIRECTIVES = "default-src 'self';"
    + "img-src 'self' https://*.openstreetmap.se https://*.basemaps.cartocdn.com;"
    + "font-src 'self' data:;"
    + "style-src 'self' 'unsafe-inline';"
    + "object-src 'none'";

  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;

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
      .antMatchers(HttpMethod.OPTIONS, "/api/v1/**").permitAll()
      .antMatchers(HttpMethod.GET, "/api/v1/organisations/*/assets/*").permitAll()
      .antMatchers(HttpMethod.GET, "/api/v1/organisations/*/theme").permitAll()
      .antMatchers("/api/v1/geocodes/**").permitAll()
      .antMatchers("/api/v1/logout").permitAll();

    http.headers()
      .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN)
      .and()
      .frameOptions().deny()
      .contentSecurityPolicy(POLICY_DIRECTIVES)
      .and()
      .httpStrictTransportSecurity();

    http.csrf().disable();

    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .authorizeRequests()
      .antMatchers(
        "/api/v1/dashboards/**",
        "/api/v1/gateways/**",
        "/api/v1/map-markers/**",
        "/api/v1/measurements/**",
        "/api/v1/meter-definitions/**",
        "/api/v1/meters/**",
        "/api/v1/selections/**",
        "/api/v1/summary/**",
        "/api/v1/user/selections/**"
      )
      .hasAnyRole(SUPER_ADMIN.role, MVP_ADMIN.role, MVP_USER.role)
      .antMatchers("/api/v1/users/**", "/api/v1/organisations/**")
      .hasAnyRole(SUPER_ADMIN.role, MVP_ADMIN.role, MVP_USER.role, OTC_ADMIN.role)
      .and()
      .authorizeRequests().antMatchers("/api/v1/**").authenticated()
      .and()
      .httpBasic().authenticationEntryPoint(new RestAuthenticationEntryPoint())
      .and()
      .addFilterAfter(new TokenAuthenticationFilter(tokenService), BasicAuthenticationFilter.class);
  }

  @Override
  public UserDetailsService userDetailsService() {
    return userDetailsService;
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
