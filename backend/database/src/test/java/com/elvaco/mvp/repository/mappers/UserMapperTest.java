package com.elvaco.mvp.repository.mappers;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.entity.user.OrganisationEntity;
import com.elvaco.mvp.entity.user.UserEntity;
import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static com.elvaco.mvp.entity.user.RoleEntity.admin;
import static com.elvaco.mvp.entity.user.RoleEntity.superAdmin;
import static com.elvaco.mvp.entity.user.RoleEntity.user;
import static com.elvaco.mvp.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.fixture.Entities.ELVACO_ENTITY;
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
    userMapper = new UserMapper(modelMapper, new OrganisationMapper());
  }

  @Test
  public void userEntityMustHaveRoles() {
    UserEntity userEntity = new UserEntity();
    userEntity.organisation = ELVACO_ENTITY;
    userEntity.roles = singletonList(user());

    List<Role> roles = userMapper.toDomainModel(userEntity).roles;

    assertThat(roles).containsExactly(USER);
  }

  @Test
  public void mapsUserEntityWithMoreThanOneRole() {
    User user = userMapper.toDomainModel(createUserEntity());

    assertThat(user.id).isEqualTo(1);
    assertThat(user.roles).containsExactly(USER, SUPER_ADMIN);
  }

  @Test
  public void mappingUserDtoToEntityShouldHavePassword() {
    UserEntity userEntity = userMapper.toEntity(createUser());

    assertThat(userEntity.password).isEqualTo("letmein");
  }

  @Test
  public void mapUserDtoToEntity() {
    User user = createUser();

    UserEntity userEntity = userMapper.toEntity(user);

    assertThat(userEntity).isEqualTo(new UserEntity(
      user.id,
      user.name,
      user.email,
      "letmein",
      new OrganisationEntity(
        user.organisation.id,
        user.organisation.name,
        user.organisation.code
      ),
      asList(admin(), user())
    ));
  }

  private User createUser() {
    return new User(
      1L,
      "john doh",
      "a@b.com",
      "letmein",
      ELVACO,
      asList(ADMIN, USER)
    );
  }

  private UserEntity createUserEntity() {
    UserEntity userEntity = new UserEntity();
    userEntity.id = 1L;
    userEntity.name = "John Doh";
    userEntity.password = "letmein";
    userEntity.email = "a@b.com";
    userEntity.organisation = ELVACO_ENTITY;
    userEntity.roles = asList(user(), superAdmin());
    return userEntity;
  }
}
