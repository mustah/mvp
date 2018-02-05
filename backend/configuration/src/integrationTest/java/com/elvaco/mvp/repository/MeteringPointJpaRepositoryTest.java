package com.elvaco.mvp.repository;

import java.util.Date;

import com.elvaco.mvp.dto.propertycollection.PropertyCollectionDto;
import com.elvaco.mvp.dto.propertycollection.UserPropertyDto;
import com.elvaco.mvp.entity.meteringpoint.LocationEntity;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.repository.jpa.MeteringPointJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class MeteringPointJpaRepositoryTest extends IntegrationTest {

  Long meteringPointId;
  @Autowired
  private MeteringPointJpaRepository meteringPointJpaRepository;

  @Before
  public void setUp() {
    MeteringPointEntity mp = new MeteringPointEntity();
    mp.created = new Date();
    mp.propertyCollection
      .put("user", new UserPropertyDto("abc123", "Under construction"))
      .putArray("numbers", asList(1, 2, 3, 17));
    LocationEntity locationEntity = new LocationEntity();
    locationEntity.confidence = 1.0;
    locationEntity.latitude = 1.0;
    locationEntity.longitude = 2.0;
    mp.setLocation(locationEntity);
    meteringPointId = meteringPointJpaRepository.save(mp).id;
  }

  @Test
  public void locationIsPersisted() {
    MeteringPointEntity foundEntity = meteringPointJpaRepository.findOne(meteringPointId);
    assertThat(foundEntity.getLocation().confidence).isEqualTo(1.0);
    assertThat(foundEntity.getLocation().latitude).isEqualTo(1.0);
    assertThat(foundEntity.getLocation().longitude).isEqualTo(2.0);
  }

  @Test
  public void isNotContainedInPropertyCollection() {
    UserPropertyDto user = new UserPropertyDto("12cccx123");
    PropertyCollectionDto requestModel = new PropertyCollectionDto(user);

    assertThat(meteringPointJpaRepository.containsInPropertyCollection(requestModel)).isEmpty();
  }

  @Test
  public void containsInPropertyCollection() {
    PropertyCollectionDto requestModel = new PropertyCollectionDto(new UserPropertyDto("abc123"));

    assertThat(meteringPointJpaRepository.containsInPropertyCollection(requestModel)).isNotEmpty();
  }

  @Test
  public void containsProjectInPropertyCollection() {
    UserPropertyDto user = new UserPropertyDto();
    user.project = "Under construction";
    PropertyCollectionDto requestModel = new PropertyCollectionDto(user);

    assertThat(meteringPointJpaRepository.containsInPropertyCollection(requestModel)).isNotEmpty();
  }

  @Test
  public void containsFullUserPropertyCollection() {
    UserPropertyDto user = new UserPropertyDto("abc123", "Under construction");
    PropertyCollectionDto requestModel = new PropertyCollectionDto(user);

    assertThat(meteringPointJpaRepository.containsInPropertyCollection(requestModel)).isNotEmpty();
  }

  @Test
  public void fullUserRequestModelDoesNotContainInPropertyCollection() {
    UserPropertyDto user = new UserPropertyDto("abc123", "building does not exist yet");
    PropertyCollectionDto requestModel = new PropertyCollectionDto(user);

    assertThat(meteringPointJpaRepository.containsInPropertyCollection(requestModel)).isEmpty();
  }

  @Test
  public void fieldNameExistsAtTopLevelJson() {
    assertThat(meteringPointJpaRepository.existsInPropertyCollection("user")).isNotEmpty();
  }

  @Test
  public void fieldNameDoesNotExistAtTopLevelJson() {
    assertThat(meteringPointJpaRepository.existsInPropertyCollection("top")).isEmpty();
  }
}
