package com.elvaco.mvp.repository;

import java.util.Arrays;

import com.elvaco.mvp.dto.propertycollection.PropertyCollectionDto;
import com.elvaco.mvp.dto.propertycollection.UserPropertyDto;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.entity.meteringpoint.PropertyCollection;
import com.elvaco.mvp.repository.jpa.MeteringPointJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class MeteringPointJpaRepositoryTest extends IntegrationTest {

  @Autowired
  private MeteringPointJpaRepository meteringPointJpaRepository;

  @Before
  public void setUp() {
    MeteringPointEntity mp = new MeteringPointEntity();
    mp.propertyCollection = new PropertyCollection()
      .put("user", new UserPropertyDto("abc123", "Under construction"))
      .putArray("numbers", Arrays.asList(1, 2, 3, 17));
    meteringPointJpaRepository.save(mp);
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