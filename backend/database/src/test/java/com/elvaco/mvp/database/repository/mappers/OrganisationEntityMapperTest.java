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

import static com.elvaco.mvp.core.util.Json.OBJECT_MAPPER;
import static com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper.toDomainModel;
import static com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper.toEntity;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class OrganisationEntityMapperTest {

  @Test
  public void toEntity_noParent() {
    UUID id = randomUUID();
    assertThat(toEntity(Organisation.of("organisation slug", id))).isEqualToComparingFieldByField(
      OrganisationEntity.builder()
        .id(id)
        .name("organisation slug")
        .slug("organisation-slug")
        .shortPrefix(null)
        .externalId("organisation slug")
        .build()
    );
  }

  @Test
  public void toEntity_noParent_shortPrefixBlank() {
    UUID id = randomUUID();
    assertThat(toEntity(Organisation.of(
      "organisation slug",
      id,
      ""
    ))).isEqualToComparingFieldByField(
      OrganisationEntity.builder()
        .id(id)
        .name("organisation slug")
        .slug("organisation-slug")
        .shortPrefix(null)
        .externalId("organisation slug")
        .build()
    );
  }

  @Test
  public void toEntity_noParent_shortPrefix() {
    UUID id = randomUUID();
    assertThat(toEntity(Organisation.of(
      "organisation slug",
      id,
      "prefix"
    ))).isEqualToComparingFieldByField(
      OrganisationEntity.builder()
        .id(id)
        .name("organisation slug")
        .slug("organisation-slug")
        .shortPrefix("prefix")
        .externalId("organisation slug")
        .build()
    );
  }

  @Test
  public void toEntity_noParent_shortPrefixOnlySpaces() {
    UUID id = randomUUID();
    assertThat(toEntity(Organisation.of(
      "organisation slug",
      id,
      "    "
    ))).isEqualToComparingFieldByField(
      OrganisationEntity.builder()
        .id(id)
        .name("organisation slug")
        .slug("organisation-slug")
        .shortPrefix(null)
        .externalId("organisation slug")
        .build()
    );
  }

  @Test
  public void toEntity_withParent() {
    var id = randomUUID();
    var parentId = randomUUID();
    var selectionId = randomUUID();
    var selectionOwnerId = randomUUID();
    var selectionOwnerOrganisationId = randomUUID();
    var parent = Organisation.of("parent slug", parentId);
    var selection = UserSelection.builder()
      .id(selectionId)
      .ownerUserId(selectionOwnerId)
      .organisationId(selectionOwnerOrganisationId)
      .name("selection")
      .selectionParameters(createJsonNode())
      .build();
    var subOrganisation = Organisation.subOrganisation("organisation slug", parent, selection)
      .id(id)
      .build();

    OrganisationEntity entity = toEntity(subOrganisation);

    assertThat(entity)
      .isEqualToComparingFieldByField(
        OrganisationEntity.builder()
          .id(id)
          .name("organisation slug")
          .slug("organisation-slug")
          .externalId("organisation slug")
          .parent(OrganisationEntity.builder()
            .id(parentId)
            .name("parent slug")
            .slug("parent-slug")
            .externalId("parent slug")
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
        .name("organisation slug")
        .slug("organisation-slug")
        .externalId("organisation slug")
        .build()
    )).isEqualTo(Organisation.of("organisation slug", id));
  }

  @Test
  public void toDomainModel_withParent() {
    var id = randomUUID();
    var parentId = randomUUID();
    var selectionId = randomUUID();
    var selectionOwnerId = randomUUID();
    var selectionOwnerOrganisationId = randomUUID();

    var subOrganisation = Organisation.subOrganisation(
      "organisation slug",
      Organisation.of("parent slug", parentId),
      UserSelection.builder()
        .id(selectionId)
        .ownerUserId(selectionOwnerId)
        .organisationId(selectionOwnerOrganisationId)
        .name("selection")
        .selectionParameters(createJsonNode())
        .build()
    )
      .id(id)
      .build();

    assertThat(toDomainModel(
      OrganisationEntity.builder()
        .id(id)
        .name("organisation slug")
        .slug("organisation-slug")
        .externalId("organisation slug")
        .parent(OrganisationEntity.builder()
          .id(parentId)
          .name("parent slug")
          .slug("parent-slug")
          .externalId("parent slug")
          .build())
        .selection(new UserSelectionEntity(
          selectionId,
          selectionOwnerId,
          "selection",
          toJsonField(createJsonNode()),
          selectionOwnerOrganisationId
        ))
        .build()
    )).isEqualTo(subOrganisation);
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
