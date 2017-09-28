package com.elvaco.mvp.repository;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elvaco.mvp.dto.properycollection.PropertyCollectionDTO;
import com.elvaco.mvp.dto.properycollection.UserPropertyDTO;
import com.elvaco.mvp.testdata.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

public class MeteringPointRepositoryTest extends IntegrationTest {

  @Autowired
  private MeteringPointRepository repository;

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
