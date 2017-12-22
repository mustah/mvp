package com.elvaco.mvp.api;

import java.util.List;

import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionControllerTest extends IntegrationTest {

  @Test
  public void findAllCollections() {
    int numCollections = restClient()
      .loginWith("user", "password")
      .get("/collections", List.class)
      .getBody()
      .size();

    assertThat(numCollections).isGreaterThanOrEqualTo(3);
  }
}
