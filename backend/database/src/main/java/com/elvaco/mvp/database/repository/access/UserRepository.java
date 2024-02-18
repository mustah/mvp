package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Password;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.exception.EmailAddressAlreadyExists;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.security.PasswordEncoder;
import com.elvaco.mvp.database.entity.user.UserEntity;
import com.elvaco.mvp.database.repository.jpa.UserJpaRepository;
import com.elvaco.mvp.database.repository.mappers.UserEntityMapper;

import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import static com.elvaco.mvp.database.repository.mappers.UserEntityMapper.toDomainModel;
import static com.elvaco.mvp.database.repository.mappers.UserEntityMapper.toEntity;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class UserRepository implements Users {

  private final UserJpaRepository userJpaRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public List<User> findAll() {
    return userJpaRepository.findAllByOrderByOrganisationNameAscNameAsc().stream()
      .map(UserEntityMapper::toDomainModel)
      .toList();
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return userJpaRepository.findByEmail(email)
      .map(UserEntityMapper::toDomainModel);
  }

  @Override
  public Optional<User> findById(UUID id) {
    return userJpaRepository.findById(id)
      .map(UserEntityMapper::toDomainModel);
  }

  @Override
  public Optional<String> findPasswordByUserId(UUID userId) {
    return userJpaRepository.findPasswordById(userId).map(Password::getPassword);
  }

  @Override
  public User save(User user) {
    try {
      UserEntity entity = toEntity(user.withPassword(passwordEncoder.encode(user.password)));
      return toDomainModel(userJpaRepository.save(entity));
    } catch (DataIntegrityViolationException e) {
      if (e.getCause() instanceof ConstraintViolationException) {
        throw new EmailAddressAlreadyExists();
      } else {
        throw e;
      }
    }
  }

  @Override
  public User update(User user) {
    return toDomainModel(userJpaRepository.save(toEntity(user)));
  }

  @Override
  public void deleteById(UUID id) {
    userJpaRepository.deleteById(id);
  }

  @Override
  public List<User> findByRole(Role role) {
    return userJpaRepository.findByRoles_Role(role.role).stream()
      .map(UserEntityMapper::toDomainModel)
      .toList();
  }

  @Override
  public List<User> findByOrganisationId(UUID organisationId) {
    return userJpaRepository.findByOrganisationIdOrOrganisation_ParentId(
      organisationId,
      organisationId
    ).stream()
      .map(UserEntityMapper::toDomainModel)
      .toList();
  }
}
