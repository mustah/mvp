package com.elvaco.mvp.web.mapper;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.web.dto.OrganisationDto;
import com.elvaco.mvp.web.dto.UserDto;
import com.elvaco.mvp.web.dto.UserWithPasswordDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toList;

@Component
public class UserMapper {

  private final ModelMapper modelMapper;

  @Autowired
  public UserMapper(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  public UserDto toDto(User user) {
    UserDto userDto = modelMapper.map(user, UserDto.class);
    userDto.roles = user.roles
      .stream()
      .map(r -> r.role)
      .collect(toList());
    return userDto;
  }

  public User toDomainModel(UserDto userDto) {
    return new User(
      userDto.id,
      userDto.name,
      userDto.email,
      organisationOf(userDto.organisation),
      rolesOf(userDto.roles)
    );
  }

  public User toDomainModel(UserWithPasswordDto userDto) {
    return new User(
      userDto.id,
      userDto.name,
      userDto.email,
      userDto.password,
      organisationOf(userDto.organisation),
      rolesOf(userDto.roles)
    );
  }

  private Organisation organisationOf(OrganisationDto organisation) {
    return new Organisation(organisation.id, organisation.name, organisation.code);
  }

  private List<Role> rolesOf(List<String> roles) {
    return roles.stream()
      .map(Role::new)
      .collect(toList());
  }
}
