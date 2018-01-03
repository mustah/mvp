package com.elvaco.mvp.config;

import com.elvaco.mvp.access.UserRepository;
import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.repository.UserJpaRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class DataProviderConfig {

  private final UserJpaRepository userJpaRepository;
  private final ModelMapper modelMapper;

  @Autowired
  DataProviderConfig(UserJpaRepository userJpaRepository, ModelMapper modelMapper) {
    this.userJpaRepository = userJpaRepository;
    this.modelMapper = modelMapper;
  }

  @Bean
  public Users users() {
    return new UserRepository(userJpaRepository, modelMapper);
  }
}
