package com.elvaco.mvp.configuration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
/* StandardPasswordEncoder is deprecated, since it's not secure. We're well aware of this, but we
want to use it anyways in this @Configuration to speed up testing */
@SuppressWarnings("deprecation")
class InMemoryConfig {

  @Bean
  PasswordEncoder inMemoryPasswordEncoder() {
    return new org.springframework.security.crypto.password.StandardPasswordEncoder();
  }
}
