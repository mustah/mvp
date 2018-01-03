package com.elvaco.mvp.config;

import javax.persistence.EntityManager;

import com.elvaco.mvp.core.Roles;
import com.elvaco.mvp.repository.jpa.MeteringPointRepository;
import com.elvaco.mvp.repository.jpa.h2.H2MeteringPointRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@H2
@Configuration
class H2Config {

  private final EntityManager entityManager;

  @Autowired
  H2Config(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Bean
  public UserDetailsService userDetailsService() {
    InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
    manager.createUser(User.withUsername("user")
                         .password("password")
                         .roles(Roles.USER)
                         .build());
    manager.createUser(User.withUsername("evanil@elvaco.se")
                         .password("eva123")
                         .roles(Roles.USER)
                         .build());
    manager.createUser(User.withUsername("hansjo@elvaco.se")
                         .password("hanna123")
                         .roles(Roles.USER, Roles.ADMIN)
                         .build());
    manager.createUser(User.withUsername("emitir@elvaco.se")
                         .password("emil123")
                         .roles(Roles.USER, Roles.ADMIN)
                         .build());
    manager.createUser(User.withUsername("a")
                         .password("a")
                         .roles(Roles.USER, Roles.ADMIN)
                         .build());
    return manager;
  }

  @Bean
  public MeteringPointRepository meteringPointRepository() {
    return new H2MeteringPointRepository(entityManager);
  }
}
