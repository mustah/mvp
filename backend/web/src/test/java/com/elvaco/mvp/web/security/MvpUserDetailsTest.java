package com.elvaco.mvp.web.security;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.domainmodels.UserSelection.IdNamedDto;
import com.elvaco.mvp.core.domainmodels.UserSelection.SelectionParametersDto;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import org.junit.Test;

import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.MARVEL;
import static com.elvaco.mvp.testing.fixture.UserTestData.organisationBuilder;
import static com.elvaco.mvp.testing.fixture.UserTestData.userBuilder;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class MvpUserDetailsTest {

  @Test
  public void shouldHaveOrganisationIdOfSubOrganisation() {
    var subOrganisationId = randomUUID();
    var userId = randomUUID();
    var subOrganisation = organisationBuilder()
      .id(subOrganisationId)
      .parent(MARVEL)
      .selection(UserSelection.builder()
        .id(randomUUID())
        .ownerUserId(userId)
        .organisationId(subOrganisationId)
        .name("selection")
        .build()
      )
      .build();

    AuthenticatedUser authenticatedUser =
      authenticatedUserFrom(userBuilder().id(userId).organisation(subOrganisation));

    assertThat(authenticatedUser.getParentOrganisationId()).isEqualTo(MARVEL.getId());
    assertThat(authenticatedUser.getOrganisationId()).isEqualTo(subOrganisationId);
  }

  @Test
  public void shouldHaveOrganisationIdWhenUserDoesNotBelongToSubOrganisation() {
    var organisation = organisationBuilder().build();

    AuthenticatedUser authenticatedUser =
      authenticatedUserFrom(userBuilder().organisation(organisation));

    assertThat(authenticatedUser.getOrganisationId()).isEqualTo(organisation.getId());
    assertThat(authenticatedUser.getParentOrganisationId()).isNull();
  }

  @Test
  public void parentOrganisationDoesNotHaveSelectionParameters() {
    var organisation = organisationBuilder().build();

    AuthenticatedUser authenticatedUser =
      authenticatedUserFrom(userBuilder().organisation(organisation));

    assertThat(authenticatedUser.selectionParameters()).isNull();
  }

  @Test
  public void subOrganisationHasSelectionParametersWithOneFacilityId() {
    var facilities = "{\"facilities\": [{\"id\": \"demo1\", \"name\": \"demo1\"}]}";
    var selectionParameters = toJsonNode(facilities);
    var organisation = organisationBuilder()
      .parent(MARVEL)
      .selection(UserSelection.builder()
        .selectionParameters(selectionParameters)
        .build())
      .build();

    AuthenticatedUser authenticatedUser =
      authenticatedUserFrom(userBuilder().organisation(organisation));

    assertThat(authenticatedUser.selectionParameters())
      .isEqualTo(new SelectionParametersDto(
        List.of(new IdNamedDto("demo1", "demo1")),
        null
      ));
  }

  @Test
  public void subOrganisationHasSelectionParametersWithTwoCities() {
    var facilities = "{\"cities\": [{\"id\": \"sverige,kungsbacka\", \"name\": \"kungsbacka\", "
      + "\"country\": {\"id\": \"sverige\", \"name\": \"sverige\"}, \"selected\": true}, "
      + "{\"id\": \"sverige,stockholm\", \"name\": \"stockholm\", "
      + "\"country\": {\"id\": \"sverige\", \"name\": \"sverige\"}, \"selected\": true}]}";
    var selectionParameters = toJsonNode(facilities);
    var organisation = organisationBuilder()
      .parent(MARVEL)
      .selection(UserSelection.builder()
        .selectionParameters(selectionParameters)
        .build())
      .build();

    AuthenticatedUser authenticatedUser =
      authenticatedUserFrom(userBuilder().organisation(organisation));

    assertThat(authenticatedUser.selectionParameters())
      .isEqualTo(new SelectionParametersDto(
        null,
        List.of(
          new IdNamedDto("sverige,kungsbacka", "kungsbacka"),
          new IdNamedDto("sverige,stockholm", "stockholm")
        )
      ));
  }

  private static MvpUserDetails authenticatedUserFrom(UserBuilder organisation) {
    return new MvpUserDetails(organisation.build(), randomUUID().toString());
  }
}
