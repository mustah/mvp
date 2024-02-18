package com.elvaco.mvp.database.repository.mappers;

import java.util.Collection;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.database.entity.user.RoleEntity;
import com.elvaco.mvp.database.entity.user.UserEntity;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserEntityMapper {

  public static User toDomainModel(UserEntity userEntity) {
    return new User(
      userEntity.id,
      userEntity.name,
      userEntity.email,
      userEntity.password,
      userEntity.language,
      OrganisationEntityMapper.toDomainModel(userEntity.organisation),
      rolesFrom(userEntity.roles)
    );
  }

  public static UserEntity toEntity(User user) {
    return new UserEntity(
      user.getId(),
      user.name,
      user.email,
      user.password,
      user.language,
      OrganisationEntityMapper.toEntity(user.organisation),
      rolesFrom(user.roles)
    );
  }

  private static List<RoleEntity> rolesFrom(List<Role> roles) {
    return roles.stream()
      .map(r -> new RoleEntity(r.role))
      .toList();
  }

  private static List<Role> rolesFrom(Collection<RoleEntity> roles) {
    return roles.stream()
      .map(r -> new Role(r.role))
      .toList();
  }
}
