package com.elvaco.mvp.web;

import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.SelectionTreeDto;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import static com.elvaco.mvp.testing.fixture.LocationTestData.kungsbacka;
import static com.elvaco.mvp.testing.fixture.LocationTestData.oslo;
import static com.elvaco.mvp.testing.fixture.LocationTestData.stockholm;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class SelectionTreeControllerTest extends IntegrationTest {

  @Test
  public void getResponseOk() {
    given(logicalMeter().location(kungsbacka().build()));
    given(logicalMeter().location(stockholm().build()));

    var response = asSuperAdmin()
      .get("/selection-tree", SelectionTreeDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void getFilteredCity() {
    given(logicalMeter().location(kungsbacka().build()));
    given(logicalMeter().location(kungsbacka().address("kabelgatan 2").build()));
    given(logicalMeter().location(stockholm().build()));

    var response = asSuperAdmin()
      .get("/selection-tree?city=sverige,kungsbacka", SelectionTreeDto.class)
      .getBody();

    assertThat(response.cities)
      .flatExtracting(c -> c.addresses)
      .extracting(a -> a.name)
      .containsExactlyInAnyOrder("kabelgatan 1", "kabelgatan 2");
  }

  @Test
  public void getFilteredAddress() {
    given(logicalMeter().location(kungsbacka().build()));
    given(logicalMeter().location(kungsbacka().address("kabelgatan 2").build()));
    given(logicalMeter().location(kungsbacka().address("kabelgatan 3").build()));
    given(logicalMeter().location(oslo().address("kabelgatan 3").build()));

    var response = asSuperAdmin()
      .get("/selection-tree?address=sverige,kungsbacka,kabelgatan+2", SelectionTreeDto.class)
      .getBody();

    assertThat(response.cities)
      .flatExtracting(c -> c.addresses)
      .extracting(a -> a.name)
      .containsExactly("kabelgatan 2");
  }
}
