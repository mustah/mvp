package com.elvaco.mvp.database.repository.mappers;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import org.junit.Test;

import static com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper.toDomainModel;
import static com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper.toEntity;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationEntityMapperTest {

  @Test
  public void toEntity_noParent() {
    UUID id = randomUUID();
    assertThat(toEntity(new Organisation(
      id,
      "organisation",
      "organisation-slug",
      "organisation-external-id"
    ))).isEqualTo(
      new OrganisationEntity(
        id, "organisation", "organisation-slug", "organisation-external-id"
      )
    );
  }

  @Test
  public void toEntity_withParent() {
    UUID id = randomUUID();
    UUID parentId = randomUUID();
    assertThat(toEntity(new Organisation(
      id,
      "organisation",
      "organisation-slug",
      "organisation-external-id",
      new Organisation(
        parentId,
        "parent",
        "parent-slug",
        "parent-external-id"
      )
    ))).isEqualTo(
      new OrganisationEntity(
        id,
        "organisation",
        "organisation-slug",
        "organisation-external-id",
        new OrganisationEntity(parentId, "parent", "parent-slug", "parent-external-id")
      )
    );
  }

  @Test
  public void toDomainModel_noParent() {
    UUID id = randomUUID();
    assertThat(toDomainModel(
      new OrganisationEntity(
        id, "organisation", "organisation-slug", "organisation-external-id"
      )
    )).isEqualTo(
      new Organisation(
        id,
        "organisation",
        "organisation-slug",
        "organisation-external-id"
      )
    );
  }

  @Test
  public void toDomainModel_withParent() {
    UUID id = randomUUID();
    UUID parentId = randomUUID();
    assertThat(toDomainModel(
      new OrganisationEntity(
        id,
        "organisation",
        "organisation-slug",
        "organisation-external-id",
        new OrganisationEntity(parentId, "parent", "parent-slug", "parent-external-id")
      )
    )).isEqualTo(
      new Organisation(
        id,
        "organisation",
        "organisation-slug",
        "organisation-external-id",
        new Organisation(
          parentId,
          "parent",
          "parent-slug",
          "parent-external-id"
        )
      )
    );
  }
}
