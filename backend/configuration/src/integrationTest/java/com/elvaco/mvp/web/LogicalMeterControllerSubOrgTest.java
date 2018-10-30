package com.elvaco.mvp.web;

import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.spi.repository.UserSelections;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.PagedLogicalMeterDto;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static com.elvaco.mvp.testing.fixture.UserSelectionTestData.CITIES_JSON_STRING;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class LogicalMeterControllerSubOrgTest extends IntegrationTest {

  @Autowired
  private LogicalMeters logicalMeters;

  @Autowired
  private PhysicalMeters physicalMeters;

  @Autowired
  private UserSelections userSelections;

  @After
  public void tearDown() {
    removeNonRootOrganisations();
    physicalMeterJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
    userSelectionJpaRepository.deleteAll();
  }

  @Test
  public void excludeMetersWithUnknownCities() {
    var userSelection = userSelections.save(UserSelection.builder()
      .id(randomUUID())
      .name("a-user-selection")
      .ownerUserId(context().superAdmin.id)
      .organisationId(context().organisationId())
      .selectionParameters(toJsonNode(CITIES_JSON_STRING))
      .build());

    organisations.save(Organisation.builder()
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
      .build());

    physicalMeters.save(physicalMeterBuilder()
      .logicalMeterId(logicalMeterWithUnknownCity.id)
      .build());

    var logicalMeterWithCity = logicalMeters.save(LogicalMeter.builder()
      .externalId("ex2")
      .organisationId(context().organisationId())
      .location(kungsbacka().build())
      .build());

    physicalMeters.save(physicalMeterBuilder()
      .logicalMeterId(logicalMeterWithCity.id)
      .build());

    Url url = Url.builder()
      .path("/meters")
      .build();

    Page<PagedLogicalMeterDto> result = asSuperAdmin()
      .getPage(url, PagedLogicalMeterDto.class);

    assertThat(result.getContent()).extracting("id").containsExactly(logicalMeterWithCity.id);
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

  // TODO[!must!] I have a branch where usages of these are removed to LocationsTestData.java
  private static LocationBuilder kungsbacka() {
    return new LocationBuilder()
      .country("sverige")
      .city("kungsbacka")
      .address("kabelgatan 1")
      .longitude(11.123)
      .latitude(12.345)
      .confidence(1.0);
  }
}
