package com.elvaco.mvp.web;

import java.util.UUID;

import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.RestClient;
import com.elvaco.mvp.web.dto.OrganisationDto;
import com.elvaco.mvp.web.dto.SubOrganisationRequestDto;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class SubOrganisationControllerTest extends IntegrationTest {

  @Autowired
  Organisations organisations;

  @After
  public void tearDown() {
    organisations.findAll().stream()
      .filter(organisation -> !organisation.id.equals(ELVACO.id))
      .sorted((o1, o2) -> {
        if (o1.parent != null) {
          return o2.parent == null ? -1 : 0;
        } else if (o2.parent != null) {
          return 1;
        }
        return 0;
      })
      .forEach(organisation -> organisations.deleteById(organisation.id));

  }

  @Test
  public void create() {
    SubOrganisationRequestDto subOrganisation = createSubOrganisationRequest();
    ResponseEntity<OrganisationDto> request = createNew(
      asTestSuperAdmin(), context().organisationId(), subOrganisation
    );

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  public void createForNonExistentParentOrganisationFails() {
    SubOrganisationRequestDto subOrganisation = createSubOrganisationRequest();
    ResponseEntity<OrganisationDto> request = createNew(
      asTestSuperAdmin(), randomUUID(), subOrganisation
    );

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void create_disallowRegularUser() {
    SubOrganisationRequestDto subOrganisation = createSubOrganisationRequest();
    ResponseEntity<OrganisationDto> request = createNew(
      asTestUser(), context().organisationId(), subOrganisation
    );

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void create_disallowOrganisationAdmin() {
    SubOrganisationRequestDto subOrganisation = createSubOrganisationRequest();
    ResponseEntity<OrganisationDto> request = createNew(
      asTestAdmin(), context().organisationId(), subOrganisation
    );

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void create_linksParentToSubOrganisation() {
    SubOrganisationRequestDto subOrganisation = createSubOrganisationRequest();
    ResponseEntity<OrganisationDto> request = createNew(
      asTestSuperAdmin(), context().organisationId(), subOrganisation
    );

    OrganisationDto dto = request.getBody();

    OrganisationDto expectedParent = new OrganisationDto(
      context().organisationId(),
      context().organisation().name,
      context().organisation().slug
    );
    assertThat(dto).isEqualToIgnoringGivenFields(
      new OrganisationDto(null, "sub", "sub-slug", expectedParent),
      "id"
    );
  }

  private SubOrganisationRequestDto createSubOrganisationRequest() {
    return new SubOrganisationRequestDto(
      "sub",
      "sub-slug",
      randomUUID()
    );
  }

  private ResponseEntity<OrganisationDto> createNew(
    RestClient restClient, UUID parentId, SubOrganisationRequestDto subOrganisation
  ) {
    return restClient.post(
      "/organisations/" + parentId + "/sub-organisations",
      subOrganisation,
      OrganisationDto.class
    );
  }
}
