package com.elvaco.mvp.repository.access;

import com.elvaco.mvp.core.Roles;
import com.elvaco.mvp.core.dto.OrganisationDto;
import com.elvaco.mvp.core.dto.UserDto;
import com.elvaco.mvp.entity.user.OrganisationEntity;
import com.elvaco.mvp.entity.user.RoleEntity;
import com.elvaco.mvp.entity.user.UserEntity;

import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class UserMapperTest {

  private UserMapper userMapper;

  @Before
  public void setUp() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(AccessLevel.PUBLIC);
    userMapper = new UserMapper(modelMapper);
  }

  @Test
  public void userEntityMustHaveRoles() {
    UserEntity userEntity = new UserEntity();
    userEntity.roles = singletonList(new RoleEntity(Roles.USER));

    UserDto userDto = userMapper.toDto(userEntity);

    assertThat(userDto.roles).containsExactly(Roles.USER);
  }

  @Test
  public void mapsUserEntityWithMoreThanOneRole() {
    UserEntity userEntity = new UserEntity();
    userEntity.id = 1L;
    userEntity.name = "John Doh";
    userEntity.password = "letmein";
    userEntity.email = "a@b.com";
    userEntity.organisation = new OrganisationEntity(1L, "Elvaco", "elvaco");
    userEntity.roles = asList(new RoleEntity(Roles.USER), new RoleEntity(Roles.SUPER_ADMIN));

    UserDto userDto = userMapper.toDto(userEntity);

    assertThat(userDto.id).isEqualTo(1);
    assertThat(userDto.roles).containsExactly(Roles.USER, Roles.SUPER_ADMIN);
  }

  @Test
  public void mapUserDtoToEntity() {
    UserDto userDto = new UserDto();
    userDto.id = 1L;
    userDto.email = "a@b.com";
    userDto.name = "john doh";
    userDto.organisation = new OrganisationDto(1L, "Elvaco", "elvaco");
    userDto.roles = asList(Roles.ADMIN, Roles.USER);

    UserEntity userEntity = userMapper.toEntity(userDto);

    assertThat(userEntity).isEqualTo(new UserEntity(
      userDto.id,
      userDto.name,
      userDto.email,
      null,
      new OrganisationEntity(
        userDto.organisation.id,
        userDto.organisation.name,
        userDto.organisation.code
      ),
      asList(new RoleEntity(Roles.ADMIN), new RoleEntity(Roles.USER))
    ));
  }
}
