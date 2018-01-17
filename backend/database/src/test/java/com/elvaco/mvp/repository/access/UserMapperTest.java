package com.elvaco.mvp.repository.access;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.security.PasswordEncoder;
import com.elvaco.mvp.entity.user.OrganisationEntity;
import com.elvaco.mvp.entity.user.RoleEntity;
import com.elvaco.mvp.entity.user.UserEntity;
import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;

import static com.elvaco.mvp.core.Roles.ADMIN;
import static com.elvaco.mvp.core.Roles.SUPER_ADMIN;
import static com.elvaco.mvp.core.Roles.USER;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class UserMapperTest {

  private UserMapper userMapper;
  private PasswordEncoder passwordEncoder;

  @Before
  public void setUp() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(AccessLevel.PUBLIC);
    passwordEncoder = rawPassword -> "::" + rawPassword + "::";
    userMapper = new UserMapper(modelMapper, passwordEncoder);
  }

  @Test
  public void userEntityMustHaveRoles() {
    UserEntity userEntity = new UserEntity();
    userEntity.organisation = new OrganisationEntity(1L, "Elvaco", "elvaco");
    userEntity.roles = singletonList(new RoleEntity(USER));

    List<Role> roles = userMapper.toDomainModel(userEntity).roles;

    assertThat(roles).containsExactly(new Role(USER));
  }

  @Test
  public void mapsUserEntityWithMoreThanOneRole() {
    User user = userMapper.toDomainModel(createUserEntity());

    assertThat(user.id).isEqualTo(1);
    assertThat(user.roles).containsExactly(new Role(USER), new Role(SUPER_ADMIN));
  }

  @Test
  public void mappingUserDtoToEntityShouldHaveEncodedPassword() {
    UserEntity userEntity = userMapper.toEntity(createUser());

    assertThat(userEntity.password).isEqualTo("::letmein::");
  }

  @Test
  public void mapUserDtoToEntity() {
    User user = createUser();

    UserEntity userEntity = userMapper.toEntity(user);

    assertThat(userEntity).isEqualTo(new UserEntity(
      user.id,
      user.name,
      user.email,
      "::letmein::",
      new OrganisationEntity(
        user.organisation.id,
        user.organisation.name,
        user.organisation.code
      ),
      asList(new RoleEntity(ADMIN), new RoleEntity(USER))
    ));
  }

  private User createUser() {
    return new User(
      1L,
      "john doh",
      "a@b.com",
      "letmein",
      new Organisation(1L, "Elvaco", "elvaco"),
      asList(new Role(ADMIN), new Role(USER))
    );
  }

  private UserEntity createUserEntity() {
    UserEntity userEntity = new UserEntity();
    userEntity.id = 1L;
    userEntity.name = "John Doh";
    userEntity.password = passwordEncoder.encode("letmein");
    userEntity.email = "a@b.com";
    userEntity.organisation = new OrganisationEntity(1L, "Elvaco", "elvaco");
    userEntity.roles = asList(new RoleEntity(USER), new RoleEntity(SUPER_ADMIN));
    return userEntity;
  }
}
