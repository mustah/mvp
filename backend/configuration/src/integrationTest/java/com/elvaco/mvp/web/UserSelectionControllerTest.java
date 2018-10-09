package com.elvaco.mvp.web;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.database.entity.meter.JsonField;
import com.elvaco.mvp.database.entity.selection.UserSelectionEntity;
import com.elvaco.mvp.database.repository.jpa.UserSelectionJpaRepository;
import com.elvaco.mvp.database.repository.mappers.UserSelectionEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.IntegrationTestFixtureContext;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.UserSelectionDto;
import com.elvaco.mvp.web.mapper.UserSelectionDtoMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class UserSelectionControllerTest extends IntegrationTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Autowired
  private UserSelectionJpaRepository repository;

  @After
  public void tearDown() {
    repository.deleteAll();
  }

  @Test
  public void findByIdForCurrentUser() throws IOException {
    IntegrationTestFixtureContext context = context();

    UserSelectionDto selection1 = createSelection(
      context.user,
      "Kungsbacka",
      (ObjectNode) OBJECT_MAPPER.readTree("{\"test\":\"some json data here\"}")
    );

    ResponseEntity<UserSelectionDto> response = as(context.user)
      .get("/user/selections/" + selection1.id, UserSelectionDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id).isEqualTo(selection1.id);
    assertThat(response.getBody().selectionParameters).isEqualTo(selection1.selectionParameters);
  }

  @Test
  public void findAllSelectionsForCurrentUser() throws IOException {
    IntegrationTestFixtureContext context = context();

    UserSelectionDto selection1 = createSelection(
      context.user,
      "Kungsbacka",
      (ObjectNode) OBJECT_MAPPER.readTree("{\"test\":\"some json data here\"}")
    );

    UserSelectionDto selection2 = createSelection(
      context.user,
      "Varberg",
      (ObjectNode) OBJECT_MAPPER.readTree("{\"test\":\"some json data here2\"}")
    );

    ResponseEntity<List<UserSelectionDto>> response = as(context.user)
      .getList("/user/selections", UserSelectionDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().size()).isEqualTo(2);
    assertThat(response.getBody().get(0).id).isEqualTo(selection1.id);
    assertThat(response.getBody().get(0).selectionParameters)
      .isEqualTo(selection1.selectionParameters);
    assertThat(response.getBody().get(1).id).isEqualTo(selection2.id);
    assertThat(response.getBody().get(1).selectionParameters)
      .isEqualTo(selection2.selectionParameters);
  }

  @Test
  public void userCanNotAccessOtherUsersSelections() throws IOException {
    IntegrationTestFixtureContext context = context();

    UserSelectionDto selection1 = createSelection(
      context.user,
      "Kungsbacka",
      (ObjectNode) OBJECT_MAPPER.readTree("{\"test\":\"some json data here\"}")
    );

    UserSelectionDto selection2 = createSelection(
      context.admin,
      "Kungsbacka",
      (ObjectNode) OBJECT_MAPPER.readTree("{\"test\":\"Json stuff here\"}")
    );

    ResponseEntity<List<UserSelectionDto>> responseFindAll = as(context.admin)
      .getList("/user/selections", UserSelectionDto.class);

    assertThat(responseFindAll.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseFindAll.getBody().size()).isEqualTo(1);
    assertThat(responseFindAll.getBody().get(0).id).isEqualTo(selection2.id);
    assertThat(responseFindAll.getBody().get(0).selectionParameters)
      .isEqualTo(selection2.selectionParameters);

    ResponseEntity<UserSelectionDto> responseFindOne = as(context.admin)
      .get("/user/selections/" + selection1.id, UserSelectionDto.class);

    assertThat(responseFindOne.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void userCanCreateSelection() throws IOException {
    ObjectNode data = (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Varberg\"}");

    UserSelectionDto userSelectionDto = new UserSelectionDto(
      null,
      context().user.id,
      "My selection",
      data,
      context().user.organisation.id
    );

    ResponseEntity<UserSelectionDto> post = as(context().user).post(
      "/user/selections",
      userSelectionDto,
      UserSelectionDto.class
    );

    assertThat(post.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(post.getBody().id).isNotNull();

    Optional<UserSelectionEntity> selection = repository.findByIdAndOwnerUserIdAndOrganisationId(
      post.getBody().id,
      context().user.id,
      context().user.organisation.id
    );

    assertThat(selection).isPresent();
    assertThat(selection.get().selectionParameters.getJson()).isEqualTo(data);
  }

  @Test
  public void userCanUpdateSelection() throws IOException {
    ObjectNode originalData = (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Varberg\"}");
    ObjectNode newData = (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Rolfstorp\"}");

    UserSelectionDto userSelectionDto = createSelection(
      context().user,
      "My selection",
      originalData
    );

    userSelectionDto.selectionParameters = newData;

    ResponseEntity<UserSelectionDto> response = as(context().user).put(
      "/user/selections",
      userSelectionDto,
      UserSelectionDto.class
    );

    UserSelectionDto updatedThroughApi = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatedThroughApi.id).isNotNull();
    assertThat(updatedThroughApi.selectionParameters).isEqualTo(newData);

    Optional<UserSelectionEntity> selectionInDatabase = repository
      .findByIdAndOwnerUserIdAndOrganisationId(
        updatedThroughApi.id,
        context().user.id,
        context().user.organisation.id
      );

    assertThat(selectionInDatabase).isPresent();
    assertThat(selectionInDatabase.get().selectionParameters.getJson()).isEqualTo(newData);
  }

  @Test
  public void userCanNotOverwriteOtherUsersSelection() throws IOException {
    ObjectNode originalData = (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Varberg\"}");
    ObjectNode changedData = (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Rolfstorp\"}");

    UserSelectionDto userSelectionDto = createSelection(
      context().user,
      "My selection",
      originalData
    );

    userSelectionDto.selectionParameters = changedData;

    ResponseEntity<ErrorMessageDto> put = as(context().admin).put(
      "/user/selections",
      userSelectionDto,
      ErrorMessageDto.class
    );

    assertThat(put.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    Optional<UserSelectionEntity> selectionUser = repository
      .findByIdAndOwnerUserIdAndOrganisationId(
        userSelectionDto.id,
        context().user.id,
        context().user.organisation.id
      );

    assertThat(selectionUser).isPresent();
    assertThat(selectionUser.get().selectionParameters.getJson()).isEqualTo(originalData);
  }

  @Test
  public void loggedInUserIsOwnerIndependentOfUserInPayloadWhenCreating() throws IOException {
    User userInPayload = context().user;
    User apiUser = context().admin;

    ObjectNode selectionJson = (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Varberg\"}");
    UserSelectionDto userSelectionDto = new UserSelectionDto(
      null,
      userInPayload.id,
      "My selection",
      selectionJson,
      userInPayload.organisation.id
    );

    ResponseEntity<UserSelectionDto> response = as(apiUser).post(
      "/user/selections",
      userSelectionDto,
      UserSelectionDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().id).isNotNull();
    assertThat(response.getBody().ownerUserId).isEqualTo(apiUser.id);

    Optional<UserSelectionEntity> selection = repository.findByIdAndOwnerUserIdAndOrganisationId(
      response.getBody().id,
      apiUser.id,
      apiUser.organisation.id
    );

    assertThat(selection).isPresent();
    assertThat(selection.get().selectionParameters.getJson()).isEqualTo(selectionJson);
  }

  @Test
  public void userCanDeleteOwnedSelections() throws IOException {
    UserSelectionDto userSelectionDto = createSelection(
      context().user,
      "My selection",
      (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Varberg\"}")
    );

    ResponseEntity<UserSelectionDto> post = as(context().user).delete(
      "/user/selections/" + userSelectionDto.id,
      UserSelectionDto.class
    );

    assertThat(post.getBody()).isEqualTo(userSelectionDto);
    assertThat(repository.findById(userSelectionDto.id)).isEmpty();
  }

  @Test
  public void userCanNotDeleteOtherUsersSelections() throws IOException {
    UserSelectionDto adminsSelectionDto = createSelection(
      context().admin,
      "Admins selection",
      (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Varberg\"}")
    );

    ResponseEntity<ErrorMessageDto> post = as(context().user).delete(
      "/user/selections/" + adminsSelectionDto.id,
      ErrorMessageDto.class
    );

    assertThat(post.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(post.getBody().message)
      .isEqualTo("Unable to find user selection with ID '" + adminsSelectionDto.id + "'");

    assertThat(repository.findById(adminsSelectionDto.id)).isNotNull();
  }

  private UserSelectionDto createSelection(
    User user,
    String name,
    ObjectNode jsonData
  ) throws IOException {
    UserSelectionEntity entity = repository.save(new UserSelectionEntity(
      UUID.randomUUID(),
      user.id,
      name,
      new JsonField(jsonData),
      user.organisation.id
    ));

    return UserSelectionDtoMapper.toDto(UserSelectionEntityMapper.toDomainModel(entity));
  }
}
