package com.elvaco.mvp.database;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.entity.user.RoleEntity;
import com.elvaco.mvp.database.entity.user.UserEntity;
import com.elvaco.mvp.database.repository.jpa.UserJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static com.elvaco.mvp.core.util.Slugify.slugify;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OrganisationRepositoryTest extends IntegrationTest {

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Test
  public void deletingOrganisationDeletesUsers() {
    OrganisationEntity organisationEntity = organisationJpaRepository.save(
      OrganisationEntity.builder()
        .id(randomUUID())
        .name("An organisation")
        .slug(slugify("An organisation"))
        .externalId("an-organisation")
        .build());
    userJpaRepository.save(new UserEntity(
      randomUUID(),
      "user",
      "user@org.com",
      "asdf",
      Language.en,
      organisationEntity,
      singletonList(RoleEntity.mvpUser())
    ));

    assertThat(userJpaRepository.findByOrganisationId(organisationEntity.id)).hasSize(1);

    organisationJpaRepository.deleteById(organisationEntity.id);

    assertThat(userJpaRepository.findByEmail("user@org.com").isPresent()).isFalse();
  }

  @Test
  public void emptyOptionalIsNotCached() {
    String externalId = randomUUID().toString();
    assertThat(organisations.findByExternalId(externalId)).isEmpty();

    // Direct save without cache evict
    organisationJpaRepository.save(
      OrganisationEntity.builder()
        .id(randomUUID())
        .name(externalId)
        .slug(externalId)
        .externalId(externalId)
        .build());

    assertThat(organisations.findByExternalId(externalId))
      .isPresent().get()
      .extracting(o -> o.externalId)
      .isEqualTo(externalId);
  }

  @Test
  public void duplicateShortPrefixNotAllowed() {
    var shortPrefix = "hello";
    organisationJpaRepository.save(OrganisationEntity.builder()
      .id(randomUUID())
      .name("An organisation")
      .slug(slugify("An organisation"))
      .externalId("an-organisation")
      .shortPrefix(shortPrefix)
      .build());

    assertThatThrownBy(() -> {
      organisationJpaRepository.save(OrganisationEntity.builder()
        .id(randomUUID())
        .name("Another organisation")
        .slug(slugify("Another organisation"))
        .externalId("another-organisation")
        .shortPrefix(shortPrefix)
        .build());
    }).isInstanceOf(DataIntegrityViolationException.class);
  }
}
