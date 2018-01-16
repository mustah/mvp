package com.elvaco.mvp.repository.access;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Password;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.entity.user.UserEntity;
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
  public List<User> findAll() {
    return userJpaRepository.findAll()
      .stream()
      .map(userMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return userJpaRepository.findByEmail(email)
      .map(userMapper::toDomainModel);
  }

  @Override
  public Optional<User> findById(Long id) {
    return Optional.ofNullable(userJpaRepository.findOne(id))
      .map(userMapper::toDomainModel);
  }

  @Override
  public Optional<Password> findPasswordByUserId(Long userId) {
    return userJpaRepository.findPasswordById(userId);
  }

  @Override
  public User save(User user) {
    UserEntity userEntity = userJpaRepository.save(userMapper.toEntity(user));
    return userMapper.toDomainModel(userEntity);
  }

  @Override
  public void deleteById(Long id) {
    userJpaRepository.delete(id);
  }
}
