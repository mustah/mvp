package com.elvaco.mvp.web;

import java.io.IOException;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.OrganisationDto;
import com.elvaco.mvp.web.dto.UnauthorizedDto;
import com.elvaco.mvp.web.mapper.OrganisationDtoMapper;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.util.Json.OBJECT_MAPPER;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationControllerTest extends IntegrationTest {

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

  @Test
  public void superAdminFindsOrganisationById() {
    ResponseEntity<OrganisationDto> request = asSuperAdmin()
      .get("/organisations/" + secretService.id, OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(request.getBody()).isEqualTo(new OrganisationDto(
      secretService.id,
      "Secret Service",
      "secret-service"
    ));
  }

  @Test
  public void adminFindsOwnOrganisationById() {
    ResponseEntity<OrganisationDto> request = asAdmin()
      .get("/organisations/" + context().organisationId(), OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(request.getBody()).isEqualTo(new OrganisationDto(
      context().defaultOrganisation().id,
      context().defaultOrganisation().name,
      context().defaultOrganisation().slug
    ));
  }

  @Test
  public void adminFindsSubOrgOfOwnOrganisationById() {
    ResponseEntity<OrganisationDto> request = asAdmin()
      .get("/organisations/" + context().organisationId(), OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(request.getBody()).isEqualTo(new OrganisationDto(
      context().defaultOrganisation().id,
      context().defaultOrganisation().name,
      context().defaultOrganisation().slug
    ));
  }

  @Test
  public void adminDoesNotFindOtherOrganisationById() {
    ResponseEntity<OrganisationDto> request = asAdmin()
      .get("/organisations/" + theBeatles.id, OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void regularUserDoesNotFindOwnOrganisationById() {
    ResponseEntity<OrganisationDto> request = asUser()
      .get("/organisations/" + context().organisationId(), OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void superAdminFindsAllOrganisations() {
    ResponseEntity<List<OrganisationDto>> request = asSuperAdmin()
      .getList("/organisations", OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(request.getBody().size()).isGreaterThanOrEqualTo(3);
  }

  @Test
  public void adminFindsOwnOrganisationAndSubOrganisation() throws IOException {

    createUserIfNotPresent(context().admin);

    UserSelection selection = UserSelection.builder()
      .id(randomUUID())
      .organisationId(context().admin.organisation.id)
      .name("")
      .selectionParameters(OBJECT_MAPPER.readTree("{\"test\":\"test selection\"}"))
      .ownerUserId(context().admin.id)
      .build();

    userSelections.save(selection);

    Organisation scooter =
      new Organisation(
        randomUUID(),
        "Scooter",
        "faster-harder",
        "Scooter",
        context().admin.organisation,
        selection
      );

    organisations.save(scooter);

    ResponseEntity<List<OrganisationDto>> request = asAdmin()
      .getList("/organisations", OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(request.getBody()).containsExactlyInAnyOrder(
      OrganisationDtoMapper.toDto(context().admin.organisation),
      OrganisationDtoMapper.toDto(scooter)
    );
  }

  @Test
  public void regularUsersDoesNotFindOrganisations() {
    ResponseEntity<List<OrganisationDto>> request = asUser()
      .getList("/organisations", OrganisationDto.class);

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(request.getBody()).hasSize(0);
  }

  @Test
  public void superAdminCanCreateOrganisation() {
    OrganisationDto input = new OrganisationDto("Something borrowed", "something-blue");
    ResponseEntity<OrganisationDto> response = asSuperAdmin()
      .post("/organisations", input, OrganisationDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    OrganisationDto output = response.getBody();
    assertThat(output.name).isEqualTo(input.name);
    assertThat(output.id).isNotNull();
  }

  @Test
  public void adminCannotCreateOrganisation() {
    OrganisationDto input = new OrganisationDto("ich bin wieder hier", "bei-dir");
    ResponseEntity<OrganisationDto> created = asAdmin()
      .post("/organisations", input, OrganisationDto.class);

    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void regularUserCannotCreateOrganisation() {
    OrganisationDto input = new OrganisationDto("ich bin wieder hier", "bei-dir");
    ResponseEntity<OrganisationDto> created = asUser()
      .post("/organisations", input, OrganisationDto.class);

    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void superAdminCanUpdateOrganisation() {
    OrganisationDto requestModel = new OrganisationDto("OrganisationName", "org-slug");

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
    OrganisationDto organisation = new OrganisationDto(
      wayneIndustries.id,
      wayneIndustries.name,
      "batcave"
    );

    ResponseEntity<UnauthorizedDto> putResponse = asAdmin()
      .put("/organisations", organisation, UnauthorizedDto.class);

    assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(putResponse.getBody().message).isEqualTo(
      "User '" + context().admin.email + "' is not allowed to save this organisation"
    );
  }

  @Test
  public void regularUserCannotUpdateOrganisation() {
    // arrange
    ResponseEntity<OrganisationDto> original = asSuperAdmin()
      .get("/organisations/" + wayneIndustries.id, OrganisationDto.class);
    assertThat(original.getStatusCode()).isEqualTo(HttpStatus.OK);

    OrganisationDto organisation = original.getBody();
    String oldCode = "wayne-industries";
    assertThat(organisation.slug).isEqualTo(oldCode);

    // act
    organisation.slug = "batcave";
    ResponseEntity<UnauthorizedDto> putResponse = asUser()
      .put("/organisations", organisation, UnauthorizedDto.class);

    // assert
    assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    ResponseEntity<OrganisationDto> updatedDto = asSuperAdmin()
      .get("/organisations/" + wayneIndustries.id, OrganisationDto.class);
    assertThat(updatedDto.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updatedDto.getBody().slug).isEqualTo(oldCode);
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

    ResponseEntity<OrganisationDto> notReallyDeleted = asAdmin()
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

    ResponseEntity<OrganisationDto> notReallyDeleted = asUser()
      .delete("/organisations/" + secretService.id, OrganisationDto.class);
    assertThat(notReallyDeleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    ResponseEntity<OrganisationDto> shouldStillExist = asSuperAdmin()
      .get("/organisations/" + secretService.id, OrganisationDto.class);
    assertThat(shouldStillExist.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
