package com.elvaco.mvp.web.mapper;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.web.dto.UserDto;
import com.elvaco.mvp.web.dto.UserWithPasswordDto;

import lombok.experimental.UtilityClass;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

@UtilityClass
public class UserDtoMapper {

  public static UserDto toDto(User user) {
    return new UserDto(
      user.id,
      user.name,
      user.email,
      user.language,
      OrganisationDtoMapper.toDto(user.organisation),
      toRoles(user.roles)
    );
  }

  public static User toDomainModel(UserDto userDto) {
    return new User(
      userDto.id != null ? userDto.id : randomUUID(),
      userDto.name,
      userDto.email,
      null,
      userDto.language,
      OrganisationDtoMapper.toDomainModel(userDto.organisation),
      rolesFrom(userDto.roles)
    );
  }

  public static User toDomainModel(UserWithPasswordDto userDto) {
    return new User(
      userDto.id != null ? userDto.id : randomUUID(),
      userDto.name,
      userDto.email,
      userDto.password,
      userDto.language,
      OrganisationDtoMapper.toDomainModel(userDto.organisation),
      rolesFrom(userDto.roles)
    );
  }

  private static List<String> toRoles(List<Role> roles) {
    return roles.stream()
      .map(r -> r.role)
      .collect(toList());
  }

  private static List<Role> rolesFrom(List<String> roles) {
    return roles.stream()
      .map(Role::new)
      .collect(toUnmodifiableList());
  }
}
