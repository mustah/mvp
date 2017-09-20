package com.elvaco.mvp.api;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elvaco.mvp.dto.properycollection.PropertyCollectionDTO;
import com.elvaco.mvp.dto.properycollection.UserPropertyDTO;
import com.elvaco.mvp.repository.MeteringPointRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.utils.Json;

import static org.assertj.core.api.Assertions.assertThat;

public class MeteringPointControllerTest extends IntegrationTest {

  @Autowired
  private MeteringPointRepository repository;

  @Test
  public void jsonb() throws Exception {
    PropertyCollectionDTO value = new PropertyCollectionDTO();
    UserPropertyDTO user = new UserPropertyDTO();
    user.externalId = "12cccx123";
    value.user = user;
    String jsonString = Json.OBJECT_MAPPER.writeValueAsString(value);

    assertThat(repository.findByExternalId("{\"user\":{\"externalId\": \"abc\"}}")).isEqualTo(null);
  }
}
