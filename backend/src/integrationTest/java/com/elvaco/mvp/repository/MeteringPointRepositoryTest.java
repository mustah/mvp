package com.elvaco.mvp.repository;

import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.entity.meteringpoint.PropertyCollection;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elvaco.mvp.dto.propertycollection.PropertyCollectionDTO;
import com.elvaco.mvp.dto.propertycollection.UserPropertyDTO;
import com.elvaco.mvp.testdata.IntegrationTest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class MeteringPointRepositoryTest extends IntegrationTest {

  @Autowired
  private MeteringPointRepository repository;

  @Before
  public void setUp() {
    MeteringPointEntity mp = new MeteringPointEntity();
    mp.propertyCollection = new PropertyCollection()
            .put("user", new UserPropertyDTO("abc123", "Under construction"))
            .putArray("numbers", Arrays.asList(1, 2, 3, 17));
    repository.save(mp);
  }
  @Test
  public void isNotContainedInPropertyCollection() throws Exception {
    UserPropertyDTO user = new UserPropertyDTO("12cccx123");
    assertThat(repository.containsInPropertyCollection(new PropertyCollectionDTO(user))).isEmpty();
  }

  @Test
  public void containsInPropertyCollection() throws Exception {
    UserPropertyDTO user = new UserPropertyDTO("abc123");
    assertThat(repository.containsInPropertyCollection(new PropertyCollectionDTO(user))).isNotEmpty();
  }

  @Test
  public void containsProjectInPropertyCollection() {
    UserPropertyDTO user = new UserPropertyDTO();
    user.project = "Under construction";
    assertThat(repository.containsInPropertyCollection(new PropertyCollectionDTO(user))).isNotEmpty();
  }

  @Test
  public void containsFullUserPropertyCollection() {
    UserPropertyDTO user = new UserPropertyDTO("abc123", "Under construction");
    assertThat(repository.containsInPropertyCollection(new PropertyCollectionDTO(user))).isNotEmpty();
  }

  @Test
  public void fullUserRequestModelDoesNotContainInPropertyCollection() {
    UserPropertyDTO user = new UserPropertyDTO("abc123", "building does not exist yet");
    assertThat(repository.containsInPropertyCollection(new PropertyCollectionDTO(user))).isEmpty();
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
