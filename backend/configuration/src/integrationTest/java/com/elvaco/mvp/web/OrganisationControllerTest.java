package com.elvaco.mvp.web;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.OrganisationDto;
import com.elvaco.mvp.web.dto.UnauthorizedDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationControllerTest extends IntegrationTest {

  @Autowired
  private Organisations organisations;

  private Organisation secretService =
    new Organisation(
      randomUUID(),
      "Secret Service",
      "secret-service",
      "Secret Service"
    );

  private Organisation wayneIndustries =
    new Organisation(
      randomUUID(),
      "Wayne Industries",
      "wayne-industries",
      "Wayne Industries"
    );

  private Organisation theBeatles =
    new Organisation(
      randomUUID(),
      "The Beatles",
      "the-beatles",
      "The Beatles"
    );

  @Before
  public void setUp() {
    secretService = organisations.save(secretService);
    wayneIndustries = organisations.save(wayneIndustries);
    theBeatles = organisations.save(theBeatles);
  }

  @After
  public void tearDown() {
    removeNonRootOrganisations();
  }

  @Test
  public void superAdminFindsOrganisationById() {
    ResponseEntity<OrganisationDto> request = asTestSuperAdmin()
      .get("/organisations/" + secretService.id, OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(request.getBody()).isEqualTo(new OrganisationDto(
      secretService.id,
      "Secret Service",
      "secret-service"
    ));
  }

  @Test
  public void adminDoesNotFindOwnOrganisationById() {
    ResponseEntity<OrganisationDto> request = asTestAdmin()
      .get("/organisations/" + context().organisationId(), OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void adminDoesNotFindOtherOrganisationById() {
    ResponseEntity<OrganisationDto> request = asTestUser()
      .get("/organisations/" + theBeatles.id, OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void regularUserDoesNotFindOwnOrganisationById() {
    ResponseEntity<OrganisationDto> request = asTestUser()
      .get("/organisations/" + context().organisationId(), OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void superAdminFindsAllOrganisations() {
    ResponseEntity<List<OrganisationDto>> request = asTestSuperAdmin()
      .getList("/organisations", OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(request.getBody().size()).isGreaterThanOrEqualTo(3);
  }

  @Test
  public void adminDoesNotFindOrganisations() {
    ResponseEntity<List<OrganisationDto>> request = asTestUser()
      .getList("/organisations", OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(request.getBody()).hasSize(0);
  }

  @Test
  public void regularUsersDoesNotFindOrganisations() {
    ResponseEntity<List<OrganisationDto>> request = asTestUser()
      .getList("/organisations", OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(request.getBody()).hasSize(0);
  }

  @Test
  public void superAdminCanCreateOrganisation() {
    OrganisationDto input = new OrganisationDto("Something borrowed", "something-blue");
    ResponseEntity<OrganisationDto> response = asTestSuperAdmin()
      .post("/organisations", input, OrganisationDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    OrganisationDto output = response.getBody();
    assertThat(output.name).isEqualTo(input.name);
    assertThat(output.id).isNotNull();
  }

  @Test
  public void adminCannotCreateOrganisation() {
    OrganisationDto input = new OrganisationDto("ich bin wieder hier", "bei-dir");
    ResponseEntity<OrganisationDto> created = asTestAdmin()
      .post("/organisations", input, OrganisationDto.class);

    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void regularUserCannotCreateOrganisation() {
    OrganisationDto input = new OrganisationDto("ich bin wieder hier", "bei-dir");
    ResponseEntity<OrganisationDto> created = asTestUser()
      .post("/organisations", input, OrganisationDto.class);

    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void superAdminCanUpdateOrganisation() {
    OrganisationDto requestModel = new OrganisationDto("OrganisationName", "org-slug");

    ResponseEntity<OrganisationDto> response = asTestSuperAdmin()
      .post("/organisations", requestModel, OrganisationDto.class);

    OrganisationDto created = response.getBody();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(created.name).isEqualTo("OrganisationName");

    created.name = "NewName";

    asTestSuperAdmin().put("/organisations", created, OrganisationDto.class);

    ResponseEntity<OrganisationDto> updatedDto = asTestSuperAdmin()
      .get("/organisations/" + created.id, OrganisationDto.class);

    assertThat(updatedDto.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatedDto.getBody().name).isEqualTo("NewName");
  }

  @Test
  public void adminCannotUpdateOrganisation() {
    OrganisationDto organisation = new OrganisationDto(
      wayneIndustries.id,
      wayneIndustries.name,
      "batcave"
    );

    ResponseEntity<UnauthorizedDto> putResponse = asTestAdmin()
      .put("/organisations", organisation, UnauthorizedDto.class);

    assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(putResponse.getBody().message).isEqualTo(
      "User '" + context().admin.email + "' is not allowed to save this organisation"
    );
  }

  @Test
  public void regularUserCannotUpdateOrganisation() {
    // arrange
    ResponseEntity<OrganisationDto> original = asTestSuperAdmin()
      .get("/organisations/" + wayneIndustries.id, OrganisationDto.class);
    assertThat(original.getStatusCode()).isEqualTo(HttpStatus.OK);

    OrganisationDto organisation = original.getBody();
    String oldCode = "wayne-industries";
    assertThat(organisation.slug).isEqualTo(oldCode);

    // act
    organisation.slug = "batcave";
    ResponseEntity<UnauthorizedDto> putResponse = asTestUser()
      .put("/organisations", organisation, UnauthorizedDto.class);

    // assert
    assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    ResponseEntity<OrganisationDto> updatedDto = asTestSuperAdmin()
      .get("/organisations/" + wayneIndustries.id, OrganisationDto.class);
    assertThat(updatedDto.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatedDto.getBody().slug).isEqualTo(oldCode);
  }

  @Test
  public void superAdminCanDeleteOrganisation() {
    ResponseEntity<OrganisationDto> exists = asTestSuperAdmin()
      .get("/organisations/" + theBeatles.id, OrganisationDto.class);

    assertThat(exists.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<OrganisationDto> deleted = asTestSuperAdmin()
      .delete("/organisations/" + theBeatles.id, OrganisationDto.class);

    assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<OrganisationDto> shouldBeDeleted = asTestSuperAdmin()
      .get("/organisations/" + theBeatles.id, OrganisationDto.class);
    assertThat(shouldBeDeleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void adminCannotDeleteOrganisation() {
    ResponseEntity<OrganisationDto> exists = asTestSuperAdmin()
      .get("/organisations/" + secretService.id, OrganisationDto.class);
    assertThat(exists.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<OrganisationDto> notReallyDeleted = asTestAdmin()
      .delete("/organisations/" + secretService.id, OrganisationDto.class);
    assertThat(notReallyDeleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    ResponseEntity<OrganisationDto> shouldStillExist = asTestSuperAdmin()
      .get("/organisations/" + secretService.id, OrganisationDto.class);
    assertThat(shouldStillExist.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void regularUserCannotDeleteOrganisation() {
    ResponseEntity<OrganisationDto> exists = asTestSuperAdmin()
      .get("/organisations/" + secretService.id, OrganisationDto.class);
    assertThat(exists.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<OrganisationDto> notReallyDeleted = asTestUser()
      .delete("/organisations/" + secretService.id, OrganisationDto.class);
    assertThat(notReallyDeleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    ResponseEntity<OrganisationDto> shouldStillExist = asTestSuperAdmin()
      .get("/organisations/" + secretService.id, OrganisationDto.class);
    assertThat(shouldStillExist.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
