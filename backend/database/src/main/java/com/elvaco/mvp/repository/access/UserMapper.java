package com.elvaco.mvp.repository.access;

import com.elvaco.mvp.core.dto.UserDto;
import com.elvaco.mvp.entity.user.RoleEntity;
import com.elvaco.mvp.entity.user.UserEntity;

import org.modelmapper.ModelMapper;

import static java.util.stream.Collectors.toList;

public class UserMapper {

  private final ModelMapper modelMapper;

  public UserMapper(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  UserDto toDto(UserEntity userEntity) {
    UserDto userDto = modelMapper.map(userEntity, UserDto.class);
    userDto.password = null;
    userDto.roles = userEntity.roles
      .stream()
      .map(r -> r.role)
      .collect(toList());
    return userDto;
  }

  UserEntity toEntity(UserDto user) {
    UserEntity userEntity = modelMapper.map(user, UserEntity.class);
    userEntity.roles = user.roles
      .stream()
      .map(RoleEntity::new)
      .collect(toList());
    return userEntity;
  }
}
