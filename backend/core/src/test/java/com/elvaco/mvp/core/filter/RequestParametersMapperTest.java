package com.elvaco.mvp.core.filter;

import java.util.List;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.testing.fixture.MockRequestParameters;

import org.junit.Test;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class RequestParametersMapperTest {

  @Test
  public void removesNullValueParameters() {
    RequestParameters requestParameters = new MockRequestParameters()
      .setAll(RequestParameter.ORGANISATION, null);

    assertThat(RequestParametersMapper.toFilters(requestParameters)).isNotNull();
  }

  @Test
  public void canAddOrganisationParameter() {
    RequestParameters requestParameters = new MockRequestParameters()
      .setAll(RequestParameter.ORGANISATION, List.of(randomUUID().toString()));

    assertThat(RequestParametersMapper.toFilters(requestParameters)).isNotNull();
  }
}