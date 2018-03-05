package com.elvaco.mvp.database.repository.mappers;

import java.util.Collection;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.database.entity.user.RoleEntity;
import com.elvaco.mvp.database.entity.user.UserEntity;

import static java.util.stream.Collectors.toList;

public class UserMapper implements DomainEntityMapper<User, UserEntity> {

  private final OrganisationMapper organisationMapper;

  public UserMapper(OrganisationMapper organisationMapper) {
    this.organisationMapper = organisationMapper;
  }

  @Override
  public User toDomainModel(UserEntity userEntity) {
    return new User(
      userEntity.id,
      userEntity.name,
      userEntity.email,
      userEntity.password,
      organisationMapper.toDomainModel(userEntity.organisation),
      rolesOf(userEntity.roles)
    );
  }

  @Override
  public UserEntity toEntity(User user) {
    return new UserEntity(
      user.getId(),
      user.name,
      user.email,
      user.password,
      organisationMapper.toEntity(user.organisation),
      rolesOf(user.roles)
    );
  }

  private List<RoleEntity> rolesOf(List<Role> roles) {
    return roles
      .stream()
      .map(r -> new RoleEntity(r.role))
      .collect(toList());
  }

  private List<Role> rolesOf(Collection<RoleEntity> roles) {
    return roles
      .stream()
      .map(r -> new Role(r.role))
      .collect(toList());
  }
}
