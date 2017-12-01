package com.elvaco.mvp.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.elvaco.mvp.testdata.IntegrationTest;
import java.util.List;
import org.junit.Test;

public class CollectionControllerTest extends IntegrationTest {

  @Test
  public void findAllCollections() {
    List collections = restClient()
        .loginWith("user", "password")
        .get("/collections", List.class)
        .getBody();

    assertThat(collections.size()).isGreaterThanOrEqualTo(3);
  }
}
