package com.elvaco.mvp.web.mapper;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.web.dto.UserDto;
import com.elvaco.mvp.web.dto.UserWithPasswordDto;

import static com.elvaco.mvp.web.util.IdHelper.uuidOf;
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
      organisationMapper.toDto(user.organisation),
      user.roles
        .stream()
        .map(r -> r.role)
        .collect(toList())
    );
  }

  public User toDomainModel(UserDto userDto) {
    return new User(
      uuidOf(userDto.id),
      userDto.name,
      userDto.email,
      null,
      organisationMapper.toDomainModel(userDto.organisation),
      rolesOf(userDto.roles)
    );
  }

  public User toDomainModel(UserWithPasswordDto userDto) {
    return new User(
      uuidOf(userDto.id),
      userDto.name,
      userDto.email,
      userDto.password,
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
