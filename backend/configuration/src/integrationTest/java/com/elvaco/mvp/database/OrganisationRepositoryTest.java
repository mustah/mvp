package com.elvaco.mvp.database;

import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.entity.user.RoleEntity;
import com.elvaco.mvp.database.entity.user.UserEntity;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.UserJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationRepositoryTest extends IntegrationTest {

  @Autowired
  private UserJpaRepository userJpaRepository;
  @Autowired
  private OrganisationJpaRepository organisationJpaRepository;

  @Test
  public void deletingOrganisationDeletesUsers() {
    OrganisationEntity organisationEntity = organisationJpaRepository.save(new OrganisationEntity(
      "An organisation",
      "an-organisation"
    ));
    userJpaRepository.save(new UserEntity(
      null,
      "user",
      "user@org.com",
      "asdf",
      organisationEntity,
      singletonList(RoleEntity.user())
    ));

    assertThat(userJpaRepository.findByOrganisation(organisationEntity)).hasSize(1);

    organisationJpaRepository.delete(organisationEntity.id);

    assertThat(userJpaRepository.findByEmail("user@org.com").isPresent()).isFalse();
  }
}
