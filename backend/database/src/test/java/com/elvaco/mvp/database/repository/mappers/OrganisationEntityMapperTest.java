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
      new OrganisationEntity(
        id, "organisation", "organisation-slug", "organisation-external-id"
      )
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
      new UserSelection(
        selectionId,
        selectionOwnerId,
        "selection",
        createJsonNode(),
        selectionOwnerOrganisationId
      )
    ))).isEqualToComparingFieldByField(
      new OrganisationEntity(
        id,
        "organisation",
        "organisation-slug",
        "organisation-external-id",
        new OrganisationEntity(parentId, "parent", "parent-slug", "parent-external-id"),
        new UserSelectionEntity(
          selectionId,
          selectionOwnerId,
          "selection",
          toJsonField(createJsonNode()),
          selectionOwnerOrganisationId
        )
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
    UUID selectionId = randomUUID();
    UUID selectionOwnerId = randomUUID();
    UUID selectionOwnerOrganisationId = randomUUID();
    assertThat(toDomainModel(
      new OrganisationEntity(
        id,
        "organisation",
        "organisation-slug",
        "organisation-external-id",
        new OrganisationEntity(parentId, "parent", "parent-slug", "parent-external-id"),
        new UserSelectionEntity(
          selectionId,
          selectionOwnerId,
          "selection",
          toJsonField(createJsonNode()),
          selectionOwnerOrganisationId
        )
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
        ),
        new UserSelection(
          selectionId,
          selectionOwnerId,
          "selection",
          createJsonNode(),
          selectionOwnerOrganisationId
        )
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
