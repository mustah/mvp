package com.elvaco.mvp.database.repository.mappers;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.database.entity.meter.JsonField;
import com.elvaco.mvp.database.entity.selection.UserSelectionEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import static com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper.toDomainModel;
import static com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper.toEntity;
import static com.elvaco.mvp.database.util.Json.OBJECT_MAPPER;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class OrganisationEntityMapperTest {

  @Test
  public void toEntity_noParent() {
    UUID id = randomUUID();
    assertThat(toEntity(new Organisation(
      id,
      "organisation",
      "organisation-slug",
      "organisation-external-id"
    ))).isEqualToComparingFieldByField(
      OrganisationEntity.builder()
        .id(id)
        .name("organisation")
        .slug("organisation-slug")
        .externalId("organisation-external-id")
        .build()
    );
  }

  @Test
  public void toEntity_withParent() {
    UUID id = randomUUID();
    UUID parentId = randomUUID();
    UUID selectionId = randomUUID();
    UUID selectionOwnerId = randomUUID();
    UUID selectionOwnerOrganisationId = randomUUID();
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
      ),
      UserSelection.builder()
        .id(selectionId)
        .ownerUserId(selectionOwnerId)
        .organisationId(selectionOwnerOrganisationId)
        .name("selection")
        .selectionParameters(createJsonNode())
        .build()
    ))).isEqualToComparingFieldByField(
      OrganisationEntity.builder()
        .id(id)
        .name("organisation")
        .slug("organisation-slug")
        .externalId("organisation-external-id")
        .parent(OrganisationEntity.builder()
          .id(parentId)
          .name("parent")
          .slug("parent-slug")
          .externalId("parent-external-id")
          .build())
        .selection(new UserSelectionEntity(
          selectionId,
          selectionOwnerId,
          "selection",
          toJsonField(createJsonNode()),
          selectionOwnerOrganisationId
        ))
        .build()
    );
  }

  @Test
  public void toDomainModel_noParent() {
    UUID id = randomUUID();
    assertThat(toDomainModel(
      OrganisationEntity.builder()
        .id(id)
        .name("organisation")
        .slug("organisation-slug")
        .externalId("organisation-external-id")
        .build()
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
    UUID selectionId = randomUUID();
    UUID selectionOwnerId = randomUUID();
    UUID selectionOwnerOrganisationId = randomUUID();
    assertThat(toDomainModel(
      OrganisationEntity.builder()
        .id(id)
        .name("organisation")
        .slug("organisation-slug")
        .externalId("organisation-external-id")
        .parent(OrganisationEntity.builder()
          .id(parentId)
          .name("parent")
          .slug("parent-slug")
          .externalId("parent-external-id")
          .build())
        .selection(new UserSelectionEntity(
          selectionId,
          selectionOwnerId,
          "selection",
          toJsonField(createJsonNode()),
          selectionOwnerOrganisationId
        ))
        .build()
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
        ),
        UserSelection.builder()
          .id(selectionId)
          .ownerUserId(selectionOwnerId)
          .organisationId(selectionOwnerOrganisationId)
          .name("selection")
          .selectionParameters(createJsonNode())
          .build()
      )
    );
  }

  private JsonField toJsonField(JsonNode jsonNode) {
    return new JsonField((ObjectNode) jsonNode);
  }

  private JsonNode createJsonNode() {
    try {
      return OBJECT_MAPPER.readTree("{\"city\": \"Perstorp\"}");
    } catch (Exception ex) {
      fail("Failed to create JSON node", ex);
      throw new RuntimeException(ex);
    }
  }
}
