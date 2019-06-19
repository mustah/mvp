package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.entity.user.UserEntity;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Role.MVP_USER;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.database.entity.user.RoleEntity.mvpAdmin;
import static com.elvaco.mvp.database.entity.user.RoleEntity.mvpUser;
import static com.elvaco.mvp.database.entity.user.RoleEntity.superAdmin;
import static com.elvaco.mvp.testing.fixture.UserTestData.userBuilder;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class UserEntityMapperTest {

  @Test
  public void mapsUserEntityWithMoreThanOneRole() {
    UserEntity userEntity = createUserEntity();
    User user = UserEntityMapper.toDomainModel(userEntity);

    assertThat(user.getId()).isEqualTo(userEntity.getId());
    assertThat(user.roles).containsExactly(MVP_USER, SUPER_ADMIN);
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
      OrganisationEntity.builder()
        .id(user.organisation.id)
        .name(user.organisation.name)
        .slug(user.organisation.slug)
        .externalId(user.organisation.externalId)
        .build(),
      asList(mvpAdmin(), mvpUser())
    ));
  }

  private User createUser() {
    return userBuilder().organisationElvaco().build();
  }

  private UserEntity createUserEntity() {
    return new UserEntity(
      randomUUID(),
      "John Doh",
      "a@b.com",
      "letmein",
      Language.en,
      OrganisationEntity.builder()
        .id(randomUUID())
        .name("Elvaco")
        .slug("elvaco")
        .externalId("Elvaco AB")
        .build(),
      asList(mvpUser(), superAdmin())
    );
  }
}
