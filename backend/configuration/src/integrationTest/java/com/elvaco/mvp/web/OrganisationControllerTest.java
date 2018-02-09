package com.elvaco.mvp.web;

import java.util.List;

import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.OrganisationDto;
import com.elvaco.mvp.web.dto.UnauthorizedDto;

import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationControllerTest extends IntegrationTest {

  @After
  public void tearDown() {
    restClient().logout();
  }

  @Test
  public void superAdminFindsOrganisationById() {
    ResponseEntity<OrganisationDto> request = asSuperAdmin()
      .get("/organisations/3", OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);

    OrganisationDto body = request.getBody();

    assertThat(body).hasFieldOrPropertyWithValue("id", 3L);
    assertThat(body).hasFieldOrPropertyWithValue("name", "Secret Service");
    assertThat(body).hasFieldOrPropertyWithValue("code", "secret-service");
  }

  @Test
  public void adminDoesNotFindOwnOrganisationById() {
    ResponseEntity<OrganisationDto> request = asAdminOfElvaco()
      .get("/organisations/1", OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void adminDoesNotFindOtherOrganisationById() {
    ResponseEntity<OrganisationDto> request = asElvacoUser()
      .get("/organisations/4", OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void regularUserDoesNotFindOwnOrganisationById() {
    ResponseEntity<OrganisationDto> request = asElvacoUser()
      .get("/organisations/1", OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void superAdminFindsAllOrganisations() {
    ResponseEntity<List> request = asSuperAdmin()
      .get("/organisations", List.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
    /*
     * we do not teardown between every test because of the time cost. the fixtures include 2
     * organisations, but the other tests adds and delete some
     */
    assertThat(request.getBody().size()).isGreaterThanOrEqualTo(2);
  }

  @Test
  public void adminDoesNotFindOrganisations() {
    ResponseEntity<List> request = asAdminOfElvaco()
      .get("/organisations", List.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(request.getBody()).hasSize(0);
  }

  @Test
  public void regularUsersDoesNotFindOrganisations() {
    ResponseEntity<List> request = asElvacoUser()
      .get("/organisations", List.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(request.getBody()).hasSize(0);
  }

  @Test
  public void superAdminCanCreateOrganisation() {
    OrganisationDto input = new OrganisationDto();
    input.name = "Something borrowed";
    input.code = "something-blue";
    ResponseEntity<OrganisationDto> response = asSuperAdmin()
      .post("/organisations", input, OrganisationDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    OrganisationDto output = response.getBody();
    assertThat(output.name).isEqualTo(input.name);
    assertThat(output.name).isEqualTo(input.name);
    assertThat(output.id).isPositive();
  }

  @Test
  public void adminCannotCreateOrganisation() {
    OrganisationDto input = new OrganisationDto(6L, "ich bin wieder hier", "bei-dir");
    ResponseEntity<OrganisationDto> created = asAdminOfElvaco()
      .post("/organisations", input, OrganisationDto.class);

    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void regularUserCannotCreateOrganisation() {
    OrganisationDto input = new OrganisationDto(6L, "ich bin wieder hier", "bei-dir");
    ResponseEntity<OrganisationDto> created = asElvacoUser()
      .post("/organisations", input, OrganisationDto.class);

    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void superAdminCanUpdateOrganisation() {
    // arrange
    ResponseEntity<OrganisationDto> original = asSuperAdmin()
      .get("/organisations/1", OrganisationDto.class);
    assertThat(original.getStatusCode()).isEqualTo(HttpStatus.OK);

    OrganisationDto organisation = original.getBody();
    assertThat(organisation.id).isEqualTo(1L);
    assertThat(organisation.code).isEqualTo("elvaco");

    // act
    final String newCode = "ocavle";
    organisation.code = newCode;
    ResponseEntity<OrganisationDto> updateResponse = asSuperAdmin()
      .put("/organisations", organisation, OrganisationDto.class);
    assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    // assert
    ResponseEntity<OrganisationDto> updatedDto = asSuperAdmin()
      .get("/organisations/1", OrganisationDto.class);
    assertThat(updatedDto.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatedDto.getBody().code).isEqualTo(newCode);
  }

  @Test
  public void adminCannotUpdateOrganisation() {
    // arrange
    ResponseEntity<OrganisationDto> original = asSuperAdmin()
      .get("/organisations/2", OrganisationDto.class);
    assertThat(original.getStatusCode()).isEqualTo(HttpStatus.OK);

    OrganisationDto organisation = original.getBody();
    String oldCode = "wayne-industries";
    assertThat(organisation.code).isEqualTo(oldCode);

    // act
    organisation.code = "batcave";
    ResponseEntity<UnauthorizedDto> put = asAdminOfElvaco()
      .put("/organisations", organisation, UnauthorizedDto.class);

    // assert
    assertThat(put.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    ResponseEntity<OrganisationDto> updatedDto = asSuperAdmin()
      .get("/organisations/2", OrganisationDto.class);
    assertThat(updatedDto.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatedDto.getBody().code).isEqualTo(oldCode);
  }

  @Test
  public void regularUserCannotUpdateOrganisation() {
    // arrange
    ResponseEntity<OrganisationDto> original = asSuperAdmin()
      .get("/organisations/2", OrganisationDto.class);
    assertThat(original.getStatusCode()).isEqualTo(HttpStatus.OK);

    OrganisationDto organisation = original.getBody();
    String oldCode = "wayne-industries";
    assertThat(organisation.code).isEqualTo(oldCode);

    // act
    organisation.code = "batcave";
    ResponseEntity<UnauthorizedDto> put = asElvacoUser()
      .put("/organisations", organisation, UnauthorizedDto.class);

    // assert
    assertThat(put.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    ResponseEntity<OrganisationDto> updatedDto = asSuperAdmin()
      .get("/organisations/2", OrganisationDto.class);
    assertThat(updatedDto.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatedDto.getBody().code).isEqualTo(oldCode);
  }

  @Test
  public void superAdminCanDeleteOrganisation() {
    ResponseEntity<OrganisationDto> exists = asSuperAdmin()
      .get("/organisations/4", OrganisationDto.class);
    assertThat(exists.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<OrganisationDto> deleted = asSuperAdmin()
      .delete("/organisations/4", OrganisationDto.class);
    assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<OrganisationDto> shouldBeDeleted = asSuperAdmin()
      .get("/organisations/4", OrganisationDto.class);
    assertThat(shouldBeDeleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void adminCannotDeleteOrganisation() {
    ResponseEntity<OrganisationDto> exists = asSuperAdmin()
      .get("/organisations/2", OrganisationDto.class);
    assertThat(exists.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<OrganisationDto> notReallyDeleted = asAdminOfElvaco()
      .delete("/organisations/2", OrganisationDto.class);
    assertThat(notReallyDeleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    ResponseEntity<OrganisationDto> shouldStillExist = asSuperAdmin()
      .get("/organisations/2", OrganisationDto.class);
    assertThat(shouldStillExist.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void regularUserCannotDeleteOrganisation() {
    ResponseEntity<OrganisationDto> exists = asSuperAdmin()
      .get("/organisations/2", OrganisationDto.class);
    assertThat(exists.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<OrganisationDto> notReallyDeleted = asElvacoUser()
      .delete("/organisations/2", OrganisationDto.class);
    assertThat(notReallyDeleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    ResponseEntity<OrganisationDto> shouldStillExist = asSuperAdmin()
      .get("/organisations/2", OrganisationDto.class);
    assertThat(shouldStillExist.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
