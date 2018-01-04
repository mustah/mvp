package com.elvaco.mvp.repository.access;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.dto.UserDto;
import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.entity.user.UserEntity;
import com.elvaco.mvp.repository.jpa.UserJpaRepository;

import org.modelmapper.ModelMapper;

import static java.util.stream.Collectors.toList;

public class UserRepository implements Users {

  private final UserJpaRepository userJpaRepository;
  private final ModelMapper modelMapper;

  public UserRepository(UserJpaRepository userJpaRepository, ModelMapper modelMapper) {
    this.userJpaRepository = userJpaRepository;
    this.modelMapper = modelMapper;
  }

  @Override
  public List<UserDto> findAll() {
    return userJpaRepository.findAll()
      .stream()
      .map(this::toDto)
      .collect(toList());
  }

  @Override
  public Optional<UserDto> findByEmail(String email) {
    return userJpaRepository.findByEmail(email)
      .map(this::toDto);
  }

  @Override
  public Optional<UserDto> findById(Long id) {
    return Optional.ofNullable(userJpaRepository.findOne(id))
      .map(this::toDto);
  }

  @Override
  public UserDto save(UserDto user) {
    return toDto(userJpaRepository.save(toEntity(user)));
  }

  @Override
  public void deleteById(Long id) {
    userJpaRepository.delete(id);
  }

  private UserDto toDto(UserEntity user) {
    return modelMapper.map(user, UserDto.class);
  }

  private UserEntity toEntity(UserDto user) {
    return modelMapper.map(user, UserEntity.class);
  }
}
