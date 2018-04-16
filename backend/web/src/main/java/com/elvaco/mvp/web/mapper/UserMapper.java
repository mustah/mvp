package com.elvaco.mvp.web.mapper;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.web.dto.UserDto;
import com.elvaco.mvp.web.dto.UserWithPasswordDto;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

public class UserMapper {

  private final OrganisationMapper organisationMapper;

  public UserMapper(OrganisationMapper organisationMapper) {
    this.organisationMapper = organisationMapper;
  }

  public UserDto toDto(User user) {
    return new UserDto(
      user.id,
      user.name,
      user.email,
      user.language,
      organisationMapper.toDto(user.organisation),
      user.roles
        .stream()
        .map(r -> r.role)
        .collect(toList())
    );
  }

  public User toDomainModel(UserDto userDto) {
    return new User(
      userDto.id != null ? userDto.id : randomUUID(),
      userDto.name,
      userDto.email,
      null,
      userDto.language,
      organisationMapper.toDomainModel(userDto.organisation),
      rolesOf(userDto.roles)
    );
  }

  public User toDomainModel(UserWithPasswordDto userDto) {
    return new User(
      userDto.id != null ? userDto.id : randomUUID(),
      userDto.name,
      userDto.email,
      userDto.password,
      userDto.language,
      organisationMapper.toDomainModel(userDto.organisation),
      rolesOf(userDto.roles)
    );
  }

  private List<Role> rolesOf(List<String> roles) {
    return roles.stream()
      .map(Role::new)
      .collect(toList());
  }
}
