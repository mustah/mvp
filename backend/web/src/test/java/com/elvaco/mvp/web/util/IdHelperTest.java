package com.elvaco.mvp.web.util;

import java.util.UUID;

import org.junit.Test;

import static com.elvaco.mvp.web.util.IdHelper.uuidOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IdHelperTest {

  @Test
  public void throwExceptionWhenIdIsNotValidUuid() {
    String id = "asd";

    assertThatThrownBy(() -> uuidOf(id))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Invalid UUID string: " + id);
  }

  @Test
  public void shouldCreateNewUuid() {
    assertThat(uuidOf(null)).isNotNull();
  }

  @Test
  public void createUuidFromUuidString() {
    String id = UUID.randomUUID().toString();

    assertThat(uuidOf(id).toString()).isEqualTo(id);
  }
}
