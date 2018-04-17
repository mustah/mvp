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
  private static final UserSelectionDtoMapper DTO_MAPPER = new UserSelectionDtoMapper();

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
      "{\"test\":\"some json data here\"}"
    );

    ResponseEntity<UserSelectionDto> response = as(context.user)
      .get("/user/selections/" + selection1.id, UserSelectionDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id).isEqualTo(selection1.id);
    assertThat(response.getBody().data).isEqualTo(selection1.data);
  }

  @Test
  public void findAllSelectionsForCurrentUser() throws IOException {
    IntegrationTestFixtureContext context = context();

    UserSelectionDto selection1 = createSelection(
      context.user,
      "Kungsbacka",
      "{\"test\":\"some json data here\"}"
    );

    UserSelectionDto selection2 = createSelection(
      context.user,
      "Varberg",
      "{\"test\":\"some json data here2\"}"
    );

    ResponseEntity<List<UserSelectionDto>> response = as(context.user)
      .getList("/user/selections", UserSelectionDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().size()).isEqualTo(2);
    assertThat(response.getBody().get(0).id).isEqualTo(selection1.id);
    assertThat(response.getBody().get(0).data).isEqualTo(selection1.data);
    assertThat(response.getBody().get(1).id).isEqualTo(selection2.id);
    assertThat(response.getBody().get(1).data).isEqualTo(selection2.data);
  }

  @Test
  public void userCanNotAccessOtherUsersSelections() throws IOException {
    IntegrationTestFixtureContext context = context();

    UserSelectionDto selection1 = createSelection(
      context.user,
      "Kungsbacka",
      "{\"test\":\"some json data here\"}"
    );

    UserSelectionDto selection2 = createSelection(
      context.admin,
      "Kungsbacka",
      "{\"test\":\"Json stuff here\"}"
    );

    ResponseEntity<List<UserSelectionDto>> responseFindAll = as(context.admin)
      .getList("/user/selections", UserSelectionDto.class);

    assertThat(responseFindAll.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseFindAll.getBody().size()).isEqualTo(1);
    assertThat(responseFindAll.getBody().get(0).id).isEqualTo(selection2.id);
    assertThat(responseFindAll.getBody().get(0).data).isEqualTo(selection2.data);

    ResponseEntity<UserSelectionDto> responseFindOne = as(context.admin)
      .get("/user/selections/" + selection1.id, UserSelectionDto.class);

    assertThat(responseFindOne.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void userCanCreateSelection() {
    String data = "{\"city\":\"Varberg\"}";

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
    assertThat(selection.get().data.asJsonString()).isEqualTo(data);
  }

  @Test
  public void userCanUpdateSelection() throws IOException {
    String originalData = "{\"city\":\"Varberg\"}";
    String newData = "{\"city\":\"Rolfstorp\"}";

    UserSelectionDto userSelectionDto = createSelection(
      context().user,
      "My selection",
      originalData
    );

    userSelectionDto.data = newData;

    ResponseEntity<UserSelectionDto> response = as(context().user).put(
      "/user/selections",
      userSelectionDto,
      UserSelectionDto.class
    );

    UserSelectionDto updatedThroughApi = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatedThroughApi.id).isNotNull();
    assertThat(updatedThroughApi.data).isEqualTo(newData);

    Optional<UserSelectionEntity> selectionInDatabase = repository
      .findByIdAndOwnerUserIdAndOrganisationId(
        updatedThroughApi.id,
        context().user.id,
        context().user.organisation.id
      );

    assertThat(selectionInDatabase).isPresent();
    assertThat(selectionInDatabase.get().data.asJsonString()).isEqualTo(newData);
  }

  @Test
  public void userCanNotOverwriteOtherUsersSelection() throws IOException {
    final String originalData = "{\"city\":\"Varberg\"}";
    final String changedData = "{\"city\":\"Rolfstorp\"}";

    UserSelectionDto userSelectionDto = createSelection(
      context().user,
      "My selection",
      originalData
    );

    userSelectionDto.data = changedData;

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
    assertThat(selectionUser.get().data.asJsonString()).isEqualTo(originalData);
  }

  @Test
  public void savingSelectionRequiresValidJson() {
    UserSelectionDto userSelectionDto = new UserSelectionDto(
      null,
      context().user.id,
      "My selection",
      "{ \"invalid json\"}",
      context().user.organisation.id
    );

    ResponseEntity<ErrorMessageDto> post = as(context().user).post(
      "/user/selections",
      userSelectionDto,
      ErrorMessageDto.class
    );

    assertThat(post.getBody().message)
      .isEqualTo("String is not valid JSON: " + userSelectionDto.data);

    assertThat(post.getStatusCode())
      .as("Should not be possible to save selections with invalid JSON data")
      .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void updatingSelectionRequiresValidJson() throws IOException {
    String originalData = "{\"city\":\"Varberg\"}";
    UserSelectionDto userSelectionDto = createSelection(
      context().user,
      "My selection",
      originalData
    );

    userSelectionDto.data = "{bad json}";

    ResponseEntity<ErrorMessageDto> response = as(context().user).put(
      "/user/selections",
      userSelectionDto,
      ErrorMessageDto.class
    );

    assertThat(response.getBody().message)
      .isEqualTo("String is not valid JSON: " + userSelectionDto.data);

    assertThat(response.getStatusCode())
      .as("Should not be possible to update selections with invalid JSON data")
      .isEqualTo(HttpStatus.BAD_REQUEST);

    UserSelectionEntity selectionInDb = repository.findOne(userSelectionDto.id);

    assertThat(selectionInDb.data.asJsonString())
      .as("Selection data should not be updated with bad json")
      .isEqualTo(originalData);
  }

  @Test
  public void loggedInUserIsOwnerIndependentOfUserInPayloadWhenCreating() {
    User userInPayload = context().user;
    User apiUser = context().admin;

    String selectionJson = "{\"city\":\"Varberg\"}";
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
    assertThat(selection.get().data.asJsonString()).isEqualTo(selectionJson);
  }

  private UserSelectionDto createSelection(
    User user,
    String name,
    String jsonData
  ) throws IOException {
    UserSelectionEntity entity = repository.save(new UserSelectionEntity(
      UUID.randomUUID(),
      user.id,
      name,
      new JsonField((ObjectNode) OBJECT_MAPPER.readTree(jsonData)),
      user.organisation.id
    ));

    return DTO_MAPPER.toDto(UserSelectionEntityMapper.toDomainModel(entity));
  }
}
