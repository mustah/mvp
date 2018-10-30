package com.elvaco.mvp.web.util;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.web.security.MvpUserDetails;
import org.junit.Test;

import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.FACILITY;
import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.MARVEL;
import static com.elvaco.mvp.testing.fixture.UserTestData.organisationBuilder;
import static com.elvaco.mvp.testing.fixture.UserTestData.userBuilder;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class EnsureOrganisationFiltersTest {

  @Test
  public void whenUserBelongsToParentOrganisation_DoNotApplyImplicitUserSelection() {
    var organisation = organisationBuilder().build();

    RequestParameters parameters = new RequestParametersAdapter()
      .ensureOrganisationFilters(authenticatedUser(organisation));

    assertThat(parameters.getValues(FACILITY)).isEmpty();
    assertThat(parameters.getValues(CITY)).isEmpty();
  }

  @Test
  public void shouldNotReplaceParameterValues_WhenNoSelectionParameters() {
    var organisation = organisationBuilder().build();

    RequestParameters parameters = new RequestParametersAdapter()
      .add(FACILITY, "c1")
      .add(FACILITY, "c2")
      .add(CITY, "sverige,kungsbacka")
      .ensureOrganisationFilters(authenticatedUser(organisation));

    assertThat(parameters.getValues(FACILITY)).containsExactlyInAnyOrder("c1", "c2");
    assertThat(parameters.getValues(CITY)).containsExactly("sverige,kungsbacka");
  }

  @Test
  public void shouldJustAddFacilitiesFromSelectionParameters() {
    RequestParameters parameters = new RequestParametersAdapter()
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation(facilitiesJsonString())));

    assertThat(parameters.getValues(FACILITY)).containsExactlyInAnyOrder("demo1", "demo2");
  }

  @Test
  public void shouldJustAddCitiesFromSelectionParameters() {
    RequestParameters parameters = new RequestParametersAdapter()
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation(citiesJsonString())));

    assertThat(parameters.getValues(CITY))
      .containsExactlyInAnyOrder("sverige,kungsbacka", "sverige,stockholm");
  }

  @Test
  public void facilityParameterValuesAreEmpty_WhenNotInSelectionParameters() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add(FACILITY, "demo3")
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation(facilitiesJsonString())));

    assertThat(parameters.getValues(FACILITY)).isEmpty();
  }

  @Test
  public void cityParameterValuesAreEmpty_WhenNotInSelectionParameters() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add(CITY, "sverige,lund")
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation(citiesJsonString())));

    assertThat(parameters.getValues(CITY)).isEmpty();
  }

  @Test
  public void onlyIncludedFacilitiesIntersecting_SelectionParametersFacilities() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add(FACILITY, "demo2")
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation(facilitiesJsonString())));

    assertThat(parameters.getValues(FACILITY)).containsExactly("demo2");
  }

  @Test
  public void onlyIncludedCitiesIntersecting_SelectionParametersCities() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add(CITY, "sverige,stockholm")
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation(citiesJsonString())));

    assertThat(parameters.getValues(CITY)).containsExactly("sverige,stockholm");
  }

  @Test
  public void doNotIncludesFacilities_WhenSelectionParametersAreEmpty() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add(FACILITY, "c1")
      .add(FACILITY, "c2")
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation("{\"facilities\": []}")));

    assertThat(parameters.getValues(FACILITY)).containsExactlyInAnyOrder("c1", "c2");
  }

  @Test
  public void doNotIncludesCities_WhenSelectionParametersAreEmpty() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add(CITY, "sverige,stockholm")
      .add(CITY, "sverige,kungsbacka")
      .ensureOrganisationFilters(authenticatedUser(makeOrganisation("{\"cities\": []}")));

    assertThat(parameters.getValues(CITY)).containsExactlyInAnyOrder(
      "sverige,kungsbacka",
      "sverige,stockholm"
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

  private static String facilitiesJsonString() {
    return "{\"facilities\": [{\"id\": \"demo1\", \"name\": \"demo1\"}, "
      + "{\"id\": \"demo2\", \"name\": \"demo2\"}]}";
  }

  private static String citiesJsonString() {
    return "{\"cities\": [{\"id\": \"sverige,kungsbacka\", \"name\": \"kungsbacka\", "
      + "\"country\": {\"id\": \"sverige\", \"name\": \"sverige\"}, \"selected\": true}, "
      + "{\"id\": \"sverige,stockholm\", \"name\": \"stockholm\", "
      + "\"country\": {\"id\": \"sverige\", \"name\": \"sverige\"}, \"selected\": true}]}";
  }
}
