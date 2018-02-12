package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Password;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.security.PasswordEncoder;
import com.elvaco.mvp.database.repository.jpa.UserJpaRepository;
import com.elvaco.mvp.database.repository.mappers.OrganisationMapper;
import com.elvaco.mvp.database.repository.mappers.UserMapper;

import static java.util.stream.Collectors.toList;

public class UserRepository implements Users {

  private final UserJpaRepository userJpaRepository;
  private final UserMapper userMapper;
  private final OrganisationMapper organisationMapper;
  private final PasswordEncoder passwordEncoder;

  public UserRepository(
    UserJpaRepository userJpaRepository,
    UserMapper userMapper,
    OrganisationMapper organisationMapper,
    PasswordEncoder passwordEncoder
  ) {
    this.userJpaRepository = userJpaRepository;
    this.userMapper = userMapper;
    this.organisationMapper = organisationMapper;
    this.passwordEncoder = passwordEncoder;
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
  public User create(User user) {
    User userWithPassword = user.withPassword(() -> passwordEncoder.encode(user.password));
    return userMapper.toDomainModel(userJpaRepository.save(userMapper.toEntity(userWithPassword)));
  }

  @Override
  public User update(User user) {
    return userMapper.toDomainModel(userJpaRepository.save(userMapper.toEntity(user)));
  }

  @Override
  public void deleteById(Long id) {
    userJpaRepository.delete(id);
  }

  @Override
  public List<User> findByRole(Role role) {
    return userJpaRepository.findByRoles_Role(role.role).stream().map(userMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public List<User> findByOrganisation(Organisation organisation) {
    return userJpaRepository
      .findByOrganisation(organisationMapper.toEntity(organisation))
      .stream()
      .map(userMapper::toDomainModel)
      .collect(toList());
  }
}