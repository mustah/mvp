
package com.elvaco.mvp.web;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.RestClient;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.OrganisationDto;
import com.elvaco.mvp.web.dto.SubOrganisationRequestDto;
import com.elvaco.mvp.web.dto.UserSelectionDto;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.util.Json.OBJECT_MAPPER;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SubOrganisationControllerTest extends IntegrationTest {

  @Test
  public void create() {
    UserSelectionDto userSelection = createUserSelection(context().superAdmin);
    SubOrganisationRequestDto subOrganisation = createSubOrganisationRequest(userSelection.id);

    ResponseEntity<OrganisationDto> request = createNew(
      asSuperAdmin(), context().organisationId(), subOrganisation
    );

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  public void subOrganisationsUseSameEndpointAsOrganisations() {
    UserSelectionDto userSelection = createUserSelection(context().superAdmin);
    SubOrganisationRequestDto subOrganisation = createSubOrganisationRequest(userSelection.id);
    ResponseEntity<OrganisationDto> request = createNew(
      asSuperAdmin(), context().organisationId(), subOrganisation
    );

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(request.getBody()).isNotNull();
    OrganisationDto organisationDto = asSuperAdmin().get(
      "/organisations/" + request.getBody().id,
      OrganisationDto.class
    ).getBody();

    assertThat(organisationDto.parent.id).isEqualTo(context().organisationId());
  }

  @Test
  public void createForNonExistentParentOrganisationFails() {
    UserSelectionDto userSelection = createUserSelection(context().superAdmin);
    SubOrganisationRequestDto subOrganisation = createSubOrganisationRequest(userSelection.id);
    ResponseEntity<OrganisationDto> request = createNew(
      asSuperAdmin(), randomUUID(), subOrganisation
    );

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void createWithOtherUsersSelectionFails() {
    User otherSuperAdmin = given(organisation(), mvpUser().asSuperAdmin()).getUser();
    UserSelectionDto userSelection = createUserSelection(otherSuperAdmin);
    SubOrganisationRequestDto subOrganisation = createSubOrganisationRequest(userSelection.id);
    ResponseEntity<ErrorMessageDto> request = createNew(
      asSuperAdmin(), randomUUID(), subOrganisation, ErrorMessageDto.class
    );

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(request.getBody()).isNotNull();
    assertThat(request.getBody().message)
      .isEqualTo("Unable to find user selection with ID '" + userSelection.id + "'");
  }

  @Test
  public void create_disallowRegularUser() {
    UserSelectionDto userSelection = createUserSelection(context().mvpUser);
    SubOrganisationRequestDto subOrganisation = createSubOrganisationRequest(userSelection.id);
    ResponseEntity<OrganisationDto> request = createNew(
      asMvpUser(), context().organisationId(), subOrganisation
    );

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void create_allowOrganisationAdmin() {
    UserSelectionDto userSelection = createUserSelection(context().mvpAdmin);
    SubOrganisationRequestDto subOrganisation = createSubOrganisationRequest(userSelection.id);
    ResponseEntity<OrganisationDto> request = createNew(
      asMvpAdmin(), context().organisationId(), subOrganisation
    );

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    Optional<Organisation> organisation = organisations.findBySlug(subOrganisation.slug);
    assertThat(organisation.isPresent()).isTrue();
  }

  @Test
  public void create_linksParentToSubOrganisationAndSelection() {
    UserSelectionDto userSelection = createUserSelection(context().superAdmin);
    SubOrganisationRequestDto subOrganisation = createSubOrganisationRequest(userSelection.id);
    ResponseEntity<OrganisationDto> request = createNew(
      asSuperAdmin(),
      context().organisationId(),
      subOrganisation
    );

    OrganisationDto dto = request.getBody();

    OrganisationDto expectedParent = new OrganisationDto(
      context().organisationId(),
      context().defaultOrganisation().name
    );
    assertThat(dto).isEqualToIgnoringGivenFields(
      new OrganisationDto(null, "sub slug", "sub-slug", null, expectedParent, userSelection.id),
      "id"
    );
  }

  private UserSelectionDto createUserSelection(User user) {
    ObjectNode data;
    try {
      data = (ObjectNode) OBJECT_MAPPER.readTree("{\"city\":\"Perstorp\"}");
    } catch (Exception ex) {
      fail("Failed to create selection data node", ex);
      throw new RuntimeException(ex);
    }

    UserSelectionDto userSelectionDto = new UserSelectionDto(
      null,
      user.id,
      "test-selection",
      data,
      user.organisation.id
    );

    ResponseEntity<UserSelectionDto> post = as(user).post(
      "/user/selections",
      userSelectionDto,
      UserSelectionDto.class
    );

    assertThat(post.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    return post.getBody();
  }

  private SubOrganisationRequestDto createSubOrganisationRequest(UUID userSelectionId) {
    return new SubOrganisationRequestDto(
      "sub slug",
      "sub-slug",
      userSelectionId
    );
  }

  private <T> ResponseEntity<T> createNew(
    RestClient restClient,
    UUID parentId,
    SubOrganisationRequestDto subOrganisation,
    Class<T> responseClass
  ) {
    return restClient.post(
      "/organisations/" + parentId + "/sub-organisations",
      subOrganisation,
      responseClass
    );
  }

  private ResponseEntity<OrganisationDto> createNew(
    RestClient restClient,
    UUID parentId,
    SubOrganisationRequestDto subOrganisation
  ) {
    return createNew(restClient, parentId, subOrganisation, OrganisationDto.class);
  }
}
