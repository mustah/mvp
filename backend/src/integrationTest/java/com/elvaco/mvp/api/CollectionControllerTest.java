package com.elvaco.mvp.api;

import java.util.List;

import org.junit.Test;

import com.elvaco.mvp.testdata.IntegrationTest;

import static com.elvaco.mvp.testdata.RestClient.restClient;
import static org.assertj.core.api.Assertions.assertThat;

public class CollectionControllerTest extends IntegrationTest {

  @Test
  public void FindAllCollections() {
    List collections = restClient()
      .loginWith("user", "password")
      .get("/collections", List.class)
      .getBody();

    assertThat(collections.size()).isGreaterThanOrEqualTo(3);
  }
}
