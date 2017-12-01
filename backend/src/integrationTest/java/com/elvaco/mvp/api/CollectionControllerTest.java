package com.elvaco.mvp.api;

import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
