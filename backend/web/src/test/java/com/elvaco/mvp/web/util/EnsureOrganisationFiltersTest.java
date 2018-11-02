package com.elvaco.mvp.web.util;

import java.util.UUID;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.web.security.MvpUserDetails;
import org.junit.Test;

import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.FACILITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ORGANISATION;
import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.MARVEL;
import static com.elvaco.mvp.testing.fixture.UserSelectionTestData.CITIES_JSON_STRING;
import static com.elvaco.mvp.testing.fixture.UserSelectionTestData.FACILITIES_JSON_STRING;
import static com.elvaco.mvp.testing.fixture.UserTestData.organisationBuilder;
import static com.elvaco.mvp.testing.fixture.UserTestData.userBuilder;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class EnsureOrganisationFiltersTest {

  @Test
  public void whenUserBelongsToParentOrganisation_DoNotApplyImplicitUserSelection() {
    var organisation = organisationBuilder().build();

    var parameters = new RequestParametersAdapter()
      .ensureOrganisationFilters(authenticatedUser(organisation));

    assertThat(parameters.getValues(FACILITY)).isEmpty();
    assertThat(parameters.getValues(CITY)).isEmpty();
  }

  @Test
  public void shouldNotReplaceParameterValues_WhenNoSelectionParameters() {
    var organisation = organisationBuilder().build();

    var parameters = new RequestParametersAdapter()
      .add(FACILITY, "c1")
      .add(FACILITY, "c2")
      .add(CITY, "sverige,kungsbacka")
      .ensureOrganisationFilters(authenticatedUser(organisation));

    assertThat(parameters.getValues(FACILITY)).containsExactlyInAnyOrder("c1", "c2");
    assertThat(parameters.getValues(CITY)).containsExactly("sverige,kungsbacka");
  }

  @Test
  public void shouldJustAddFacilitiesFromSelectionParameters() {
    var parameters = new RequestParametersAdapter()
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation(FACILITIES_JSON_STRING)));

    assertThat(parameters.getValues(FACILITY)).containsExactlyInAnyOrder("demo1", "demo2");
  }

  @Test
  public void shouldJustAddCitiesFromSelectionParameters() {
    var parameters = new RequestParametersAdapter()
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation(CITIES_JSON_STRING)));

    assertThat(parameters.getValues(CITY))
      .containsExactlyInAnyOrder("sverige,kungsbacka", "sverige,stockholm");
  }

  @Test
  public void facilityParameterValuesAreEmpty_WhenNotInSelectionParameters() {
    var parameters = new RequestParametersAdapter()
      .add(FACILITY, "demo3")
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation(FACILITIES_JSON_STRING)));

    assertThat(parameters.getValues(FACILITY)).isEmpty();
  }

  @Test
  public void cityParameterValuesAreEmpty_WhenNotInSelectionParameters() {
    var parameters = new RequestParametersAdapter()
      .add(CITY, "sverige,lund")
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation(CITIES_JSON_STRING)));

    assertThat(parameters.getValues(CITY)).isEmpty();
  }

  @Test
  public void onlyIncludedFacilitiesIntersecting_SelectionParametersFacilities() {
    var parameters = new RequestParametersAdapter()
      .add(FACILITY, "demo2")
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation(FACILITIES_JSON_STRING)));

    assertThat(parameters.getValues(FACILITY)).containsExactly("demo2");
  }

  @Test
  public void onlyIncludedCitiesIntersecting_SelectionParametersCities() {
    var parameters = new RequestParametersAdapter()
      .add(CITY, "sverige,stockholm")
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation(CITIES_JSON_STRING)));

    assertThat(parameters.getValues(CITY)).containsExactly("sverige,stockholm");
  }

  @Test
  public void requestedOrganisations_WhenSuperAdmin() {
    var interestingOrganisations = asList(randomUUID(), randomUUID())
      .stream().map(UUID::toString).collect(toList());
    var usersOrganisation = Organisation.builder()
      .id(randomUUID())
      .slug(randomUUID().toString())
      .build();
    var parameters = new RequestParametersAdapter()
      .setAll(ORGANISATION, interestingOrganisations)
      .ensureOrganisationFilters(authenticatedSuperAdmin(usersOrganisation));

    assertThat(parameters.getValues(ORGANISATION)).containsAll(interestingOrganisations);
  }

  @Test
  public void requestedOrganisations_WhenUser() {
    var organisationsToSpyOn = asList(randomUUID(), randomUUID())
      .stream().map(UUID::toString).collect(toList());
    var usersOrganisation = Organisation.builder()
      .id(randomUUID())
      .slug(randomUUID().toString())
      .build();
    var parameters = new RequestParametersAdapter()
      .setAll(ORGANISATION, organisationsToSpyOn)
      .ensureOrganisationFilters(authenticatedUser(usersOrganisation));

    assertThat(parameters.getValues(ORGANISATION)).containsExactly(usersOrganisation.id.toString());
  }

  @Test
  public void doNotIncludesFacilities_WhenSelectionParametersAreEmpty() {
    var parameters = new RequestParametersAdapter()
      .add(FACILITY, "c1")
      .add(FACILITY, "c2")
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation("{\"facilities\": []}")));

    assertThat(parameters.getValues(FACILITY)).containsExactlyInAnyOrder("c1", "c2");
  }

  @Test
  public void doNotIncludesCities_WhenSelectionParametersAreEmpty() {
    var parameters = new RequestParametersAdapter()
      .add(CITY, "sverige,stockholm")
      .add(CITY, "sverige,kungsbacka")
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation("{\"cities\": []}")));

    assertThat(parameters.getValues(CITY)).containsExactlyInAnyOrder(
      "sverige,kungsbacka",
      "sverige,stockholm"
    );
  }

  @Test
  public void doNotIncludesUnknownCity_WhenUserSelectionCitiesIsEmpty() {
    var parameters = new RequestParametersAdapter()
      .add(CITY, "sverige,stockholm")
      .add(CITY, "unknown,unknown")
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation("{\"cities\": []}")));

    assertThat(parameters.getValues(CITY)).containsExactlyInAnyOrder("sverige,stockholm");
  }

  @Test
  public void doNotIncludesUnknownCity_WhenUserSelectionCitiesExists() {
    var parameters = new RequestParametersAdapter()
      .add(CITY, "sverige,stockholm")
      .add(CITY, "unknown,unknown")
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation(CITIES_JSON_STRING)));

    assertThat(parameters.getValues(CITY)).containsExactly("sverige,stockholm");
  }

  private static MvpUserDetails authenticatedSuperAdmin(Organisation organisation) {
    return new MvpUserDetails(
      userBuilder().organisation(organisation).asSuperAdmin().build(),
      randomUUID().toString()
    );
  }

  private static MvpUserDetails authenticatedUser(Organisation organisation) {
    return new MvpUserDetails(
      userBuilder().organisation(organisation).build(),
      randomUUID().toString()
    );
  }

  private static Organisation makeOrganisation(String jsonString) {
    return organisationBuilder()
      .parent(MARVEL)
      .selection(UserSelection.builder()
        .selectionParameters(toJsonNode(jsonString))
        .build())
      .build();
  }
}
