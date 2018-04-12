package com.elvaco.mvp.database;

import java.io.IOException;
import java.util.UUID;

import com.elvaco.mvp.database.entity.meter.JsonField;
import com.elvaco.mvp.database.entity.selection.UserSelectionEntity;
import com.elvaco.mvp.database.repository.jpa.UserSelectionJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class UserSelectionJpaRepositoryTest extends IntegrationTest {

  private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Autowired
  UserSelectionJpaRepository userSelectionJpaRepository;

  @After
  public void tearDown() {
    userSelectionJpaRepository.deleteAll();
  }

  @Test
  public void jsonbFieldTypeDetectsChanges() throws IOException {
    UUID owner = context().user.id;
    String name = "My selection";
    JsonField originalData = new JsonField((ObjectNode) OBJECT_MAPPER.readTree("{\"city\": "
      + "\"Höganäs\"}"));
    UUID organisationId = context().user.organisation.id;

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
    assertThat(saved.data).isEqualTo(entityToSave.data);
    assertThat(saved.organisationId).isEqualTo(entityToSave.organisationId);

    UserSelectionEntity fetchedInitialFromDb = userSelectionJpaRepository.findOne(saved.id);
    assertThat(fetchedInitialFromDb.data).isEqualTo(originalData);

    JsonField newData = new JsonField(
      (ObjectNode) OBJECT_MAPPER.readTree("{\"city\": \"Rolfstorp\"}")
    );
    saved.data = newData;

    UserSelectionEntity modifiedAndSaved = userSelectionJpaRepository.save(saved);

    assertThat(modifiedAndSaved.id).isEqualTo(saved.id);
    assertThat(modifiedAndSaved.data).isEqualTo(newData);

    UserSelectionEntity fetchedUpdatedFromDb = userSelectionJpaRepository.findOne(saved.id);
    assertThat(fetchedUpdatedFromDb.data).isEqualTo(newData);
  }
}
