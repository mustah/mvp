package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.RestClient;
import com.elvaco.mvp.web.dto.LocationDto;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class SelectionControllerTest extends IntegrationTest {

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private OrganisationJpaRepository organisationJpaRepository;

  @Autowired
  private LogicalMeters logicalMeters;

  private OrganisationEntity anotherOrganisation = null;

  @After
  public void tearDown() {
    logicalMeterJpaRepository.deleteAll();

    if (anotherOrganisation != null) {
      organisationJpaRepository.delete(anotherOrganisation);
      anotherOrganisation = null;
    }
  }

  @Test
  public void getLocation() {
    logicalMeters.save(newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", "extId1"));
    logicalMeters.save(newLogicalMeter("sweden", "kungsbacka", "kabelgatan 2", "extId2"));
    logicalMeters.save(newLogicalMeter("sweden", "gothenburg", "kabelgatan 3", "extId3"));
    logicalMeters.save(newLogicalMeter("finland", "helsinki", "joksigatan 2", "extId4"));

    Page<LocationDto> response = asTestUser()
      .getPage("/selections/locations", LocationDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void getLocationFilteredOnCity() {
    logicalMeters.save(newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", "extId1"));
    logicalMeters.save(newLogicalMeter("sweden", "kungsbacka", "kabelgatan 2", "extId2"));
    logicalMeters.save(newLogicalMeter("sweden", "gothenburg", "kabelgatan 3", "extId3"));
    logicalMeters.save(newLogicalMeter("finland", "helsinki", "joksigatan 2", "extId4"));

    Page<LocationDto> response = asTestUser()
      .getPage("/selections/locations?city=Kungsbacka", LocationDto.class);

    assertThat(response.getTotalElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent().get(0).city.name).isEqualTo("kungsbacka");
    assertThat(response.getContent().get(1).city.name).isEqualTo("kungsbacka");
  }

  @Test
  public void getLocationFilteredOnAddress() {
    logicalMeters.save(newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", "extId1"));
    logicalMeters.save(newLogicalMeter("sweden", "kungsbacka", "kabelgatan 2", "extId2"));
    logicalMeters.save(newLogicalMeter("sweden", "gothenburg", "snabelgatan 3", "extId3"));
    logicalMeters.save(newLogicalMeter("finland", "helsinki", "joksigatan 2", "extId4"));

    Page<LocationDto> response = asTestUser()
      .getPage("/selections/locations?address=abel&sort=address", LocationDto.class);

    List<String> addresses = asList("kabelgatan 1", "kabelgatan 2", "snabelgatan 3");

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent().get(0).address.name).isIn(addresses);
    assertThat(response.getContent().get(1).address.name).isIn(addresses);
    assertThat(response.getContent().get(2).address.name).isIn(addresses);
  }

  @Test
  public void userCanNotAccessOtherOrganisationsLocations() {
    RestClient userClient = restAsUser(context().user);

    anotherOrganisation = organisationJpaRepository.save(
      new OrganisationEntity(
        randomUUID(),
        "Another Organisation",
        "another-organisation",
        "another-organisation"
      ));

    logicalMeters.save(newLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 1",
      "extId1",
      context().user.organisation.id
    ));
    logicalMeters.save(newLogicalMeter(
      "finland",
      "helsinki",
      "joksigatan 2",
      "extId4",
      context().user.organisation.id
    ));
    logicalMeters.save(newLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 2",
      "extId2",
      anotherOrganisation.id
    ));
    logicalMeters.save(newLogicalMeter(
      "sweden",
      "gothenburg",
      "snabelgatan 3",
      "extId3",
      anotherOrganisation.id
    ));

    Page<LocationDto> response = userClient.getPage(
      "/selections/locations?address=abel",
      LocationDto.class
    );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent().get(0).address.name).isEqualTo("kabelgatan 1");
  }

  private LogicalMeter newLogicalMeter(
    String country,
    String city,
    String address,
    String externalId
  ) {
    return newLogicalMeter(country, city, address, externalId, context().getOrganisationId());
  }

  private LogicalMeter newLogicalMeter(
    String country,
    String city,
    String address,
    String externalId,
    UUID organisationId
  ) {
    Location location = new LocationBuilder()
      .country(country)
      .city(city)
      .address(address)
      .build();
    return new LogicalMeter(
      randomUUID(),
      externalId,
      organisationId,
      MeterDefinition.UNKNOWN_METER,
      ZonedDateTime.now(),
      emptyList(),
      emptyList(),
      emptyList(),
      location,
      null,
      0L, null
    );
  }
}
