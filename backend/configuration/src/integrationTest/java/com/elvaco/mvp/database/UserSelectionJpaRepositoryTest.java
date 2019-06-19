package com.elvaco.mvp.database;

import java.io.IOException;
import java.util.UUID;

import com.elvaco.mvp.database.entity.meter.JsonField;
import com.elvaco.mvp.database.entity.selection.UserSelectionEntity;
import com.elvaco.mvp.testdata.IntegrationTest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import static com.elvaco.mvp.core.util.Json.OBJECT_MAPPER;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class UserSelectionJpaRepositoryTest extends IntegrationTest {

  @Test
  public void jsonbFieldTypeDetectsChanges() throws IOException {
    UUID owner = context().mvpUser.id;
    String name = "My selection";
    JsonField originalData = new JsonField((ObjectNode) OBJECT_MAPPER.readTree("{\"city\": "
      + "\"Höganäs\"}"));
    UUID organisationId = context().mvpUser.organisation.id;

    UserSelectionEntity entityToSave = new UserSelectionEntity(
      randomUUID(),
      owner,
      name,
      originalData,
      organisationId
    );
    UserSelectionEntity saved = userSelectionJpaRepository.save(entityToSave);

    assertThat(saved.id).isNotNull();
    assertThat(saved.ownerUserId).isEqualTo(entityToSave.ownerUserId);
    assertThat(saved.name).isEqualTo(entityToSave.name);
    assertThat(saved.selectionParameters).isEqualTo(entityToSave.selectionParameters);
    assertThat(saved.organisationId).isEqualTo(entityToSave.organisationId);

    UserSelectionEntity fetchedInitialFromDb = userSelectionJpaRepository.findById(saved.id).get();
    assertThat(fetchedInitialFromDb.selectionParameters).isEqualTo(originalData);

    JsonField newData = new JsonField(
      (ObjectNode) OBJECT_MAPPER.readTree("{\"city\": \"Rolfstorp\"}")
    );
    saved.selectionParameters = newData;

    UserSelectionEntity modifiedAndSaved = userSelectionJpaRepository.save(saved);

    assertThat(modifiedAndSaved.id).isEqualTo(saved.id);
    assertThat(modifiedAndSaved.selectionParameters).isEqualTo(newData);

    UserSelectionEntity fetchedUpdatedFromDb = userSelectionJpaRepository.findById(saved.id).get();
    assertThat(fetchedUpdatedFromDb.selectionParameters).isEqualTo(newData);
  }
}
