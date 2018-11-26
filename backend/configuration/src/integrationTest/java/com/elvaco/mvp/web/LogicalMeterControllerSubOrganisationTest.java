package com.elvaco.mvp.web;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.spi.repository.UserSelections;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.PagedLogicalMeterDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static com.elvaco.mvp.testing.fixture.UserSelectionTestData.CITIES_JSON_STRING;
import static com.elvaco.mvp.testing.fixture.UserTestData.subOrgUser;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class LogicalMeterControllerSubOrganisationTest extends IntegrationTest {

  @Autowired
  private UserSelections userSelections;

  @Test
  public void excludeMetersWithUnknownCities() {
    assumeTrue(isPostgresDialect());

    var logicalMeterId = randomUUID();
    var user = subOrganisationUser(CITIES_JSON_STRING, logicalMeterId);

    var content = as(user).getPage(Url.builder()
      .path("/meters")
      .build(), PagedLogicalMeterDto.class)
      .getContent();

    assertThat(content).extracting("id").containsExactly(logicalMeterId);
  }

  @Test
  public void invalidSelectionThrowsException() {
    var user = subOrganisationUser(
      "{\"cities\":[\"sweden,perstorp\"],\"addresses\":[],"
        + "\"alarms\":[],\"manufacturers\":[],"
        + "\"productModels\":[],\"dateRange\":{\"period\":\"latest\"}}",
      randomUUID()
    );

    var response = as(user).get(
      Url.builder()
        .path("/meters")
        .build(),
      ErrorMessageDto.class
    );

    assertThat(response.getBody().message).isEqualTo(
      "Invalid configuration of sub-organisation, please contact support"
    );
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private User subOrganisationUser(String parentOrganisationsSelection, UUID logicalMeterId) {
    var userSelection = userSelections.save(UserSelection.builder()
      .id(randomUUID())
      .name("a-user-selection")
      .ownerUserId(context().superAdmin.id)
      .organisationId(context().organisationId())
      .selectionParameters(toJsonNode(parentOrganisationsSelection))
      .build());

    var subOrganisation = organisations.save(Organisation.builder()
      .name("sub-org")
      .slug("sub-org")
      .externalId("sub-org")
      .parent(context().organisation())
      .selection(userSelection)
      .build()
    );

    var logicalMeterWithUnknownCity = logicalMeters.save(LogicalMeter.builder()
      .externalId("ex1")
      .organisationId(context().organisationId())
      .utcOffset(DEFAULT_UTC_OFFSET)
      .build());

    physicalMeters.save(physicalMeterBuilder()
      .logicalMeterId(logicalMeterWithUnknownCity.id)
      .build());

    var logicalMeterWithCity = logicalMeters.save(LogicalMeter.builder()
      .id(logicalMeterId)
      .externalId("ex2")
      .organisationId(context().organisationId())
      .location(kungsbacka().build())
      .utcOffset(DEFAULT_UTC_OFFSET)
      .build());

    physicalMeters.save(physicalMeterBuilder()
      .logicalMeterId(logicalMeterWithCity.id)
      .build());

    return subOrgUser().organisation(subOrganisation).build();
  }

  private PhysicalMeter.PhysicalMeterBuilder physicalMeterBuilder() {
    return PhysicalMeter.builder()
      .organisation(context().organisation())
      .address("address-123")
      .externalId(randomUUID().toString())
      .medium(Medium.HOT_WATER.medium)
      .manufacturer("ELV1")
      .readIntervalMinutes(30);
  }
}
