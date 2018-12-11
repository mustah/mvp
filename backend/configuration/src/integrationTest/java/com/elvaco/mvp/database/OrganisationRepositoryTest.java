package com.elvaco.mvp.database;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.entity.user.RoleEntity;
import com.elvaco.mvp.database.entity.user.UserEntity;
import com.elvaco.mvp.database.repository.jpa.UserJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationRepositoryTest extends IntegrationTest {

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Test
  public void deletingOrganisationDeletesUsers() {
    OrganisationEntity organisationEntity = organisationJpaRepository.save(
      OrganisationEntity.builder()
        .id(randomUUID())
        .name("An organisation")
        .slug("an-organisation")
        .externalId("an-organisation")
        .build());
    userJpaRepository.save(new UserEntity(
      randomUUID(),
      "user",
      "user@org.com",
      "asdf",
      Language.en,
      organisationEntity,
      singletonList(RoleEntity.user())
    ));

    assertThat(userJpaRepository.findByOrganisationId(organisationEntity.id)).hasSize(1);

    organisationJpaRepository.deleteById(organisationEntity.id);

    assertThat(userJpaRepository.findByEmail("user@org.com").isPresent()).isFalse();
  }
}
