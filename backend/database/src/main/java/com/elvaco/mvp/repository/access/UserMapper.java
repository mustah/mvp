package com.elvaco.mvp.repository.access;

import java.util.Collection;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.entity.user.OrganisationEntity;
import com.elvaco.mvp.entity.user.RoleEntity;
import com.elvaco.mvp.entity.user.UserEntity;
import org.modelmapper.ModelMapper;

import static java.util.stream.Collectors.toList;

public class UserMapper implements DomainEntityMapper<User, UserEntity> {

  private final ModelMapper modelMapper;

  public UserMapper(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  @Override
  public User toDomainModel(UserEntity userEntity) {
    return new User(
      userEntity.id,
      userEntity.name,
      userEntity.email,
      userEntity.password,
      newOrganisation(userEntity.organisation),
      rolesOf(userEntity.roles)
    );
  }

  @Override
  public UserEntity toEntity(User user) {
    UserEntity userEntity = modelMapper.map(user, UserEntity.class);
    userEntity.roles = user.roles
      .stream()
      .map(r -> new RoleEntity(r.role))
      .collect(toList());
    return userEntity;
  }

  private List<Role> rolesOf(Collection<RoleEntity> roles) {
    return roles
      .stream()
      .map(r -> new Role(r.role))
      .collect(toList());
  }

  private Organisation newOrganisation(OrganisationEntity organisation) {
    return new Organisation(organisation.id, organisation.name, organisation.code);
  }
}
