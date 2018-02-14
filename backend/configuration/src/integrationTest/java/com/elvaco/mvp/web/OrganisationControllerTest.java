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

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationControllerTest extends IntegrationTest {

  @Autowired
  Organisations organisations;

  private Organisation secretService =
    new Organisation(null, "Secret Service", "secret-service");

  private Organisation wayneIndustries =
    new Organisation(null, "Wayne Industries", "wayne-industries");

  private Organisation theBeatles =
    new Organisation(null, "The Beatles", "the-beatles");


  @After
  public void tearDown() {
    organisations.findAll()
      .stream()
      .filter(organisation -> !organisation.name.equals(ELVACO.name))
      .forEach(
        (organisation) -> organisations.deleteById(organisation.id)
      );
    restClient().logout();
  }

  @Before
  public void setUp() {
    secretService = organisations.save(secretService);
    wayneIndustries = organisations.save(wayneIndustries);
    theBeatles = organisations.save(theBeatles);
  }

  @Test
  public void superAdminFindsOrganisationById() {
    ResponseEntity<OrganisationDto> request = asSuperAdmin()
      .get("/organisations/" + secretService.id, OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);

    OrganisationDto body = request.getBody();

    assertThat(body).hasFieldOrPropertyWithValue("id", secretService.id);
    assertThat(body).hasFieldOrPropertyWithValue("name", "Secret Service");
    assertThat(body).hasFieldOrPropertyWithValue("code", "secret-service");
  }

  @Test
  public void adminDoesNotFindOwnOrganisationById() {
    ResponseEntity<OrganisationDto> request = asAdminOfElvaco()
      .get("/organisations/" + ELVACO.id, OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void adminDoesNotFindOtherOrganisationById() {
    ResponseEntity<OrganisationDto> request = asElvacoUser()
      .get("/organisations/" + theBeatles.id, OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void regularUserDoesNotFindOwnOrganisationById() {
    ResponseEntity<OrganisationDto> request = asElvacoUser()
      .get("/organisations/" + ELVACO.id, OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void superAdminFindsAllOrganisations() {
    ResponseEntity<List> request = asSuperAdmin()
      .get("/organisations", List.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(request.getBody().size()).isGreaterThanOrEqualTo(3);
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
    OrganisationDto input = new OrganisationDto(null, "Something borrowed", "something-blue");
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
    OrganisationDto input = new OrganisationDto(null, "ich bin wieder hier", "bei-dir");
    ResponseEntity<OrganisationDto> created = asAdminOfElvaco()
      .post("/organisations", input, OrganisationDto.class);

    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void regularUserCannotCreateOrganisation() {
    OrganisationDto input = new OrganisationDto(null, "ich bin wieder hier", "bei-dir");
    ResponseEntity<OrganisationDto> created = asElvacoUser()
      .post("/organisations", input, OrganisationDto.class);

    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void superAdminCanUpdateOrganisation() {
    OrganisationDto requestModel = new OrganisationDto(null, "OrganisationName", "org-code");

    ResponseEntity<OrganisationDto> response = asSuperAdmin()
      .post("/organisations", requestModel, OrganisationDto.class);

    OrganisationDto created = response.getBody();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(created.name).isEqualTo("OrganisationName");

    created.name = "NewName";

    asSuperAdmin().put("/organisations", created, OrganisationDto.class);

    ResponseEntity<OrganisationDto> updatedDto = asSuperAdmin()
      .get("/organisations/" + created.id, OrganisationDto.class);

    assertThat(updatedDto.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatedDto.getBody().name).isEqualTo("NewName");
  }

  @Test
  public void adminCannotUpdateOrganisation() {
    // arrange
    ResponseEntity<OrganisationDto> original = asSuperAdmin()
      .get("/organisations/" + wayneIndustries.id, OrganisationDto.class);
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
      .get("/organisations/" + wayneIndustries.id, OrganisationDto.class);
    assertThat(updatedDto.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatedDto.getBody().code).isEqualTo(oldCode);
  }

  @Test
  public void regularUserCannotUpdateOrganisation() {
    // arrange
    ResponseEntity<OrganisationDto> original = asSuperAdmin()
      .get("/organisations/" + wayneIndustries.id, OrganisationDto.class);
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
      .get("/organisations/" + wayneIndustries.id, OrganisationDto.class);
    assertThat(updatedDto.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatedDto.getBody().code).isEqualTo(oldCode);
  }

  @Test
  public void superAdminCanDeleteOrganisation() {
    ResponseEntity<OrganisationDto> exists = asSuperAdmin()
      .get("/organisations/" + theBeatles.id, OrganisationDto.class);
    assertThat(exists.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<OrganisationDto> deleted = asSuperAdmin()
      .delete("/organisations/" + theBeatles.id, OrganisationDto.class);
    assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<OrganisationDto> shouldBeDeleted = asSuperAdmin()
      .get("/organisations/" + theBeatles.id, OrganisationDto.class);
    assertThat(shouldBeDeleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void adminCannotDeleteOrganisation() {
    ResponseEntity<OrganisationDto> exists = asSuperAdmin()
      .get("/organisations/" + secretService.id, OrganisationDto.class);
    assertThat(exists.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<OrganisationDto> notReallyDeleted = asAdminOfElvaco()
      .delete("/organisations/" + secretService.id, OrganisationDto.class);
    assertThat(notReallyDeleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    ResponseEntity<OrganisationDto> shouldStillExist = asSuperAdmin()
      .get("/organisations/" + secretService.id, OrganisationDto.class);
    assertThat(shouldStillExist.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void regularUserCannotDeleteOrganisation() {
    ResponseEntity<OrganisationDto> exists = asSuperAdmin()
      .get("/organisations/" + secretService.id, OrganisationDto.class);
    assertThat(exists.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<OrganisationDto> notReallyDeleted = asElvacoUser()
      .delete("/organisations/" + secretService.id, OrganisationDto.class);
    assertThat(notReallyDeleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    ResponseEntity<OrganisationDto> shouldStillExist = asSuperAdmin()
      .get("/organisations/" + secretService.id, OrganisationDto.class);
    assertThat(shouldStillExist.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
