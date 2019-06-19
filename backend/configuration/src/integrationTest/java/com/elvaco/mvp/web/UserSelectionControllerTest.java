package com.elvaco.mvp.web;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.database.entity.meter.JsonField;
import com.elvaco.mvp.database.entity.selection.UserSelectionEntity;
import com.elvaco.mvp.database.repository.mappers.UserSelectionEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.IntegrationTestFixtureContext;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.UserSelectionDto;
import com.elvaco.mvp.web.mapper.UserSelectionDtoMapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.util.Json.OBJECT_MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

public class UserSelectionControllerTest extends IntegrationTest {

  @Test
  public void findByIdForCurrentUser() throws IOException {
    IntegrationTestFixtureContext context = context();

    UserSelectionDto selection1 = createSelection(
      context.mvpUser,
      "Kungsbacka",
      (ObjectNode) OBJECT_MAPPER.readTree("{\"test\":\"some json data here\"}")
    );

    ResponseEntity<UserSelectionDto> response = asMvpUser()
      .get("/user/selections/" + selection1.id, UserSelectionDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id).isEqualTo(selection1.id);
    assertThat(response.getBody().selectionParameters).isEqualTo(selection1.selectionParameters);
  }

  @Test
  public void findAllSelectionsForCurrentUser() throws IOException {
    IntegrationTestFixtureContext context = context();

    UserSelectionDto selection1 = createSelection(
      context.mvpUser,
      "Kungsbacka",
      (ObjectNode) OBJECT_MAPPER.readTree("{\"test\":\"some json data here\"}")
    );

    UserSelectionDto selection2 = createSelection(
      context.mvpUser,
      "Varberg",
      (ObjectNode) OBJECT_MAPPER.readTree("{\"test\":\"some json data here2\"}")
    );

    ResponseEntity<List<UserSelectionDto>> response = asMvpUser()
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
      context.mvpUser,
      "Kungsbacka",
      (ObjectNode) OBJECT_MAPPER.readTree("{\"test\":\"some json data here\"}")
    );

    UserSelectionDto selection2 = createSelection(
      context.mvpAdmin,
      "Kungsbacka",
      (ObjectNode) OBJECT_MAPPER.readTree("{\"test\":\"Json stuff here\"}")
    );

    ResponseEntity<List<UserSelectionDto>> responseFindAll = asMvpAdmin()
      .getList("/user/selections", UserSelectionDto.class);

    assertThat(responseFindAll.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseFindAll.getBody()).hasSize(1);
    assertThat(responseFindAll.getBody().get(0).id).isEqualTo(selection2.id);
    assertThat(responseFindAll.getBody().get(0).selectionParameters)
      .isEqualTo(selection2.selectionParameters);

    ResponseEntity<UserSelectionDto> responseFindOne = asMvpAdmin()
      .get("/user/selections/" + selection1.id, UserSelectionDto.class);

    assertThat(responseFindOne.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void userCanCreateSelection() throws IOException {
    ObjectNode data = (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Varberg\"}");

    UserSelectionDto userSelectionDto = new UserSelectionDto(
      null,
      context().mvpUser.id,
      "My selection",
      data,
      context().mvpUser.organisation.id
    );

    ResponseEntity<UserSelectionDto> post = asMvpUser().post(
      "/user/selections",
      userSelectionDto,
      UserSelectionDto.class
    );

    assertThat(post.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(post.getBody().id).isNotNull();

    Optional<UserSelectionEntity> selection = userSelectionJpaRepository
      .findByIdAndOwnerUserIdAndOrganisationId(
        post.getBody().id,
        context().mvpUser.id,
        context().mvpUser.organisation.id
      );

    assertThat(selection).isPresent();
    assertThat(selection.get().selectionParameters.getJson()).isEqualTo(data);
  }

  @Test
  public void userCanUpdateSelection() throws IOException {
    ObjectNode originalData = (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Varberg\"}");
    ObjectNode newData = (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Rolfstorp\"}");

    UserSelectionDto userSelectionDto = createSelection(
      context().mvpUser,
      "My selection",
      originalData
    );

    userSelectionDto.selectionParameters = newData;

    ResponseEntity<UserSelectionDto> response = asMvpUser().put(
      "/user/selections",
      userSelectionDto,
      UserSelectionDto.class
    );

    UserSelectionDto updatedThroughApi = response.getBody();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatedThroughApi.id).isNotNull();
    assertThat(updatedThroughApi.selectionParameters).isEqualTo(newData);

    Optional<UserSelectionEntity> selectionInDatabase = userSelectionJpaRepository
      .findByIdAndOwnerUserIdAndOrganisationId(
        updatedThroughApi.id,
        context().mvpUser.id,
        context().mvpUser.organisation.id
      );

    assertThat(selectionInDatabase).isPresent();
    assertThat(selectionInDatabase.get().selectionParameters.getJson()).isEqualTo(newData);
  }

  @Test
  public void userCanNotOverwriteOtherUsersSelection() throws IOException {
    ObjectNode originalData = (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Varberg\"}");
    ObjectNode changedData = (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Rolfstorp\"}");

    UserSelectionDto userSelectionDto = createSelection(
      context().mvpUser,
      "My selection",
      originalData
    );

    userSelectionDto.selectionParameters = changedData;

    ResponseEntity<ErrorMessageDto> put = asMvpAdmin().put(
      "/user/selections",
      userSelectionDto,
      ErrorMessageDto.class
    );

    assertThat(put.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    Optional<UserSelectionEntity> selectionUser = userSelectionJpaRepository
      .findByIdAndOwnerUserIdAndOrganisationId(
        userSelectionDto.id,
        context().mvpUser.id,
        context().mvpUser.organisation.id
      );

    assertThat(selectionUser).isPresent();
    assertThat(selectionUser.get().selectionParameters.getJson()).isEqualTo(originalData);
  }

  @Test
  public void loggedInUserIsOwnerIndependentOfUserInPayloadWhenCreating() throws IOException {
    User userInPayload = context().mvpUser;
    User apiUser = context().mvpAdmin;

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

    Optional<UserSelectionEntity> selection = userSelectionJpaRepository
      .findByIdAndOwnerUserIdAndOrganisationId(
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
      context().mvpUser,
      "My selection",
      (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Varberg\"}")
    );

    ResponseEntity<UserSelectionDto> post = asMvpUser().delete(
      "/user/selections/" + userSelectionDto.id,
      UserSelectionDto.class
    );

    assertThat(post.getBody()).isEqualTo(userSelectionDto);
    assertThat(userSelectionJpaRepository.findById(userSelectionDto.id)).isEmpty();
  }

  @Test
  public void userCanNotDeleteOtherUsersSelections() throws IOException {
    UserSelectionDto adminsSelectionDto = createSelection(
      context().mvpAdmin,
      "Admins selection",
      (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Varberg\"}")
    );

    ResponseEntity<ErrorMessageDto> post = asMvpUser().delete(
      "/user/selections/" + adminsSelectionDto.id,
      ErrorMessageDto.class
    );

    assertThat(post.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(post.getBody().message)
      .isEqualTo("Unable to find user selection with ID '" + adminsSelectionDto.id + "'");

    assertThat(userSelectionJpaRepository.findById(adminsSelectionDto.id)).isNotNull();
  }

  private UserSelectionDto createSelection(
    User user,
    String name,
    ObjectNode jsonData
  ) {
    UserSelectionEntity entity = userSelectionJpaRepository.save(new UserSelectionEntity(
      UUID.randomUUID(),
      user.id,
      name,
      new JsonField(jsonData),
      user.organisation.id
    ));

    return UserSelectionDtoMapper.toDto(UserSelectionEntityMapper.toDomainModel(entity));
  }
}
