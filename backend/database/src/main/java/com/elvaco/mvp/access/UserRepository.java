package com.elvaco.mvp.access;

import java.util.Optional;

import com.elvaco.mvp.core.dto.UserDto;
import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.entity.user.UserEntity;
import com.elvaco.mvp.repository.UserJpaRepository;

import org.modelmapper.ModelMapper;

public class UserRepository implements Users {

  private final UserJpaRepository userJpaRepository;
  private final ModelMapper modelMapper;

  public UserRepository(UserJpaRepository userJpaRepository, ModelMapper modelMapper) {
    this.userJpaRepository = userJpaRepository;
    this.modelMapper = modelMapper;
  }

  @Override
  public Optional<UserDto> findByEmail(String email) {
    return userJpaRepository.findByEmail(email)
      .map(this::toDto);
  }

  private UserDto toDto(UserEntity userEntity) {
    return modelMapper.map(userEntity, UserDto.class);
  }
}
