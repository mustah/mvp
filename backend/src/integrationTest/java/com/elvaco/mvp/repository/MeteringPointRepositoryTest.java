package com.elvaco.mvp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.elvaco.mvp.dto.propertycollection.PropertyCollectionDto;
import com.elvaco.mvp.dto.propertycollection.UserPropertyDto;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.entity.meteringpoint.PropertyCollection;
import com.elvaco.mvp.testdata.IntegrationTest;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MeteringPointRepositoryTest extends IntegrationTest {

  @Autowired
  private MeteringPointRepository repository;

  @Before
  public void setUp() {
    MeteringPointEntity mp = new MeteringPointEntity();
    mp.propertyCollection = new PropertyCollection()
        .put("user", new UserPropertyDto("abc123", "Under construction"))
        .putArray("numbers", Arrays.asList(1, 2, 3, 17));
    repository.save(mp);
  }

  @Test
  public void isNotContainedInPropertyCollection() throws Exception {
    UserPropertyDto user = new UserPropertyDto("12cccx123");
    assertThat(repository.containsInPropertyCollection(new PropertyCollectionDto(user))).isEmpty();
  }

  @Test
  public void containsInPropertyCollection() throws Exception {
    UserPropertyDto user = new UserPropertyDto("abc123");
    assertThat(repository.containsInPropertyCollection(
        new PropertyCollectionDto(user))).isNotEmpty();
  }

  @Test
  public void containsProjectInPropertyCollection() {
    UserPropertyDto user = new UserPropertyDto();
    user.project = "Under construction";
    assertThat(repository.containsInPropertyCollection(
        new PropertyCollectionDto(user))).isNotEmpty();
  }

  @Test
  public void containsFullUserPropertyCollection() {
    UserPropertyDto user = new UserPropertyDto("abc123", "Under construction");
    assertThat(repository.containsInPropertyCollection(
        new PropertyCollectionDto(user))).isNotEmpty();
  }

  @Test
  public void fullUserRequestModelDoesNotContainInPropertyCollection() {
    UserPropertyDto user = new UserPropertyDto("abc123", "building does not exist yet");
    assertThat(repository.containsInPropertyCollection(
        new PropertyCollectionDto(user))).isEmpty();
  }

  @Test
  public void fieldNameExistsAtTopLevelJson() {
    assertThat(repository.existsInPropertyCollection("user")).isNotEmpty();
  }

  @Test
  public void fieldNameDoesNotExistAtTopLevelJson() {
    assertThat(repository.existsInPropertyCollection("top")).isEmpty();
  }
}
