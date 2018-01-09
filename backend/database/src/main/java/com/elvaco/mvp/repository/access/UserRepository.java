package com.elvaco.mvp.repository.access;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.dto.UserDto;
import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.repository.jpa.UserJpaRepository;

import static java.util.stream.Collectors.toList;

public class UserRepository implements Users {

  private final UserJpaRepository userJpaRepository;
  private final UserMapper userMapper;

  public UserRepository(UserJpaRepository userJpaRepository, UserMapper userMapper) {
    this.userJpaRepository = userJpaRepository;
    this.userMapper = userMapper;
  }

  @Override
  public List<UserDto> findAll() {
    return userJpaRepository.findAll()
      .stream()
      .map(userMapper::toDto)
      .collect(toList());
  }

  @Override
  public Optional<UserDto> findByEmail(String email) {
    return userJpaRepository.findByEmail(email)
      .map(userMapper::toDto);
  }

  @Override
  public Optional<UserDto> findById(Long id) {
    return Optional.ofNullable(userJpaRepository.findOne(id))
      .map(userMapper::toDto);
  }

  @Override
  public UserDto save(UserDto user) {
    return userMapper.toDto(userJpaRepository.save(userMapper.toEntity(user)));
  }

  @Override
  public void deleteById(Long id) {
    userJpaRepository.delete(id);
  }
}
