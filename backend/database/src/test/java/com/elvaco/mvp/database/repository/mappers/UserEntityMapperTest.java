package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.entity.user.UserEntity;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;
import static com.elvaco.mvp.database.entity.user.RoleEntity.admin;
import static com.elvaco.mvp.database.entity.user.RoleEntity.superAdmin;
import static com.elvaco.mvp.database.entity.user.RoleEntity.user;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class UserEntityMapperTest {

  @Test
  public void mapsUserEntityWithMoreThanOneRole() {
    UserEntity userEntity = createUserEntity();
    User user = UserEntityMapper.toDomainModel(userEntity);

    assertThat(user.getId()).isEqualTo(userEntity.getId());
    assertThat(user.roles).containsExactly(USER, SUPER_ADMIN);
  }

  @Test
  public void mappingUserDtoToEntityShouldHavePassword() {
    UserEntity userEntity = UserEntityMapper.toEntity(createUser());

    assertThat(userEntity.password).isEqualTo("letmein");
  }

  @Test
  public void mapUserDtoToEntity() {
    User user = createUser();

    UserEntity userEntity = UserEntityMapper.toEntity(user);

    assertThat(userEntity).isEqualTo(new UserEntity(
      user.id,
      user.name,
      user.email,
      "letmein",
      Language.en,
      new OrganisationEntity(
        user.organisation.id,
        user.organisation.name,
        user.organisation.slug,
        user.organisation.externalId
      ),
      asList(admin(), user())
    ));
  }

  private User createUser() {
    return new UserBuilder()
      .name("john doh")
      .email("a@b.com")
      .password("letmein")
      .organisationElvaco()
      .roles(ADMIN, USER)
      .build();
  }

  private UserEntity createUserEntity() {
    return new UserEntity(
      randomUUID(),
      "John Doh",
      "a@b.com",
      "letmein",
      Language.en,
      new OrganisationEntity(
        randomUUID(),
        "Elvaco",
        "elvaco",
        "Elvaco AB"
      ),
      asList(user(), superAdmin())
    );
  }
}
