package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.security.TokenFactory;
import com.elvaco.mvp.database.repository.mappers.FilterToPredicateMapper;
import com.elvaco.mvp.database.repository.mappers.LogicalMeterToPredicateMapper;
import com.elvaco.mvp.database.repository.mappers.MeasurementFilterToPredicateMapper;
import com.elvaco.mvp.web.security.JpaUserDetailsService;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
class ApplicationConfig {

  private final Users users;
  private final TokenFactory tokenFactory;

  @Autowired
  ApplicationConfig(@Lazy Users users, @Lazy TokenFactory tokenFactory) {
    this.users = users;
    this.tokenFactory = tokenFactory;
  }

  @Bean
  UserDetailsService userDetailsService() {
    return new JpaUserDetailsService(users, tokenFactory);
  }

  @Bean
  FilterToPredicateMapper predicateMapper() {
    return new MeasurementFilterToPredicateMapper();
  }

  @Bean
  LogicalMeterToPredicateMapper logicalMeterToPredicateMapper() {
    return new LogicalMeterToPredicateMapper();
  }

  @Bean
  ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(AccessLevel.PUBLIC);
    return modelMapper;
  }
}
