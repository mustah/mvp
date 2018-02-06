package com.elvaco.mvp.database;

import java.util.Date;

import com.elvaco.mvp.database.dto.propertycollection.PropertyCollectionDto;
import com.elvaco.mvp.database.dto.propertycollection.UserPropertyDto;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterJpaRepositoryTest extends IntegrationTest {

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private OrganisationJpaRepository organisationRepository;

  private Long logicalMeterId;

  @Before
  public void setUp() {
    LogicalMeterEntity mp = new LogicalMeterEntity();
    mp.created = new Date();
    mp.propertyCollection
      .put("user", new UserPropertyDto("abc123", "Under construction"))
      .putArray("numbers", asList(1, 2, 3, 17));
    LocationEntity locationEntity = new LocationEntity();
    locationEntity.confidence = 1.0;
    locationEntity.latitude = 1.0;
    locationEntity.longitude = 2.0;
    mp.setLocation(locationEntity);

    mp = logicalMeterJpaRepository.save(mp);
    PhysicalMeterEntity physicalMeterEntity = new PhysicalMeterEntity(
      organisationRepository.findOne(0L),
      "123123",
      "Some medium"
    );
    physicalMeterEntity.logicalMeterId = mp.id;
    physicalMeterJpaRepository.save(physicalMeterEntity);
    logicalMeterId = mp.id;
  }

  @Test
  public void locationIsPersisted() {
    LogicalMeterEntity foundEntity = logicalMeterJpaRepository.findOne(logicalMeterId);
    assertThat(foundEntity.getLocation().confidence).isEqualTo(1.0);
    assertThat(foundEntity.getLocation().latitude).isEqualTo(1.0);
    assertThat(foundEntity.getLocation().longitude).isEqualTo(2.0);
  }

  @Test
  public void isNotContainedInPropertyCollection() {
    UserPropertyDto user = new UserPropertyDto("12cccx123");
    PropertyCollectionDto requestModel = new PropertyCollectionDto(user);

    assertThat(logicalMeterJpaRepository.containsInPropertyCollection(requestModel)).isEmpty();
  }

  @Test
  public void containsInPropertyCollection() {
    PropertyCollectionDto requestModel = new PropertyCollectionDto(new UserPropertyDto("abc123"));

    assertThat(logicalMeterJpaRepository.containsInPropertyCollection(requestModel)).isNotEmpty();
  }

  @Test
  public void containsProjectInPropertyCollection() {
    UserPropertyDto user = new UserPropertyDto();
    user.project = "Under construction";
    PropertyCollectionDto requestModel = new PropertyCollectionDto(user);

    assertThat(logicalMeterJpaRepository.containsInPropertyCollection(requestModel)).isNotEmpty();
  }

  @Test
  public void containsFullUserPropertyCollection() {
    UserPropertyDto user = new UserPropertyDto("abc123", "Under construction");
    PropertyCollectionDto requestModel = new PropertyCollectionDto(user);

    assertThat(logicalMeterJpaRepository.containsInPropertyCollection(requestModel)).isNotEmpty();
  }

  @Test
  public void fullUserRequestModelDoesNotContainInPropertyCollection() {
    UserPropertyDto user = new UserPropertyDto("abc123", "building does not exist yet");
    PropertyCollectionDto requestModel = new PropertyCollectionDto(user);

    assertThat(logicalMeterJpaRepository.containsInPropertyCollection(requestModel)).isEmpty();
  }

  @Test
  public void fieldNameExistsAtTopLevelJson() {
    assertThat(logicalMeterJpaRepository.existsInPropertyCollection("user")).isNotEmpty();
  }

  @Test
  public void fieldNameDoesNotExistAtTopLevelJson() {
    assertThat(logicalMeterJpaRepository.existsInPropertyCollection("top")).isEmpty();
  }

  @Test
  public void physicalMetersAreFetched() {
    assertThat(logicalMeterJpaRepository.findOne(logicalMeterId).physicalMeters).isNotEmpty();
  }
}
