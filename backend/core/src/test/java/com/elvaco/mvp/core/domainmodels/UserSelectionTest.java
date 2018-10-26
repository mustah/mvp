package com.elvaco.mvp.core.domainmodels;

import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.UserSelection.SelectionParametersDto;
import static com.elvaco.mvp.core.util.Json.toObject;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class UserSelectionTest {

  @Test
  public void hasNoFacilities() {
    var emptyFacilities = "{\"facilities\": []}";
    var dto = toObject(emptyFacilities, SelectionParametersDto.class);

    assertThat(dto.facilities).isEmpty();
  }

  @Test
  public void hosNoFacilitiesNode_ShouldReturnNull() {
    var dto = toObject("{}", SelectionParametersDto.class);

    assertThat(dto.facilities).isNull();
  }

  @Test
  public void facilitiesFromJson() {
    var facilities = "{\"facilities\": [{\"id\": \"C4_DEMO1\", \"name\": \"C4_DEMO1\"}]}";
    var dto = toObject(facilities, SelectionParametersDto.class);

    assertThat(dto.facilities).extracting("id").containsExactly("C4_DEMO1");
  }

  @Test
  public void ignoresUnknownProperties_SuchAsSelected() {
    var facilities =
      "{\"facilities\": [{\"id\": \"C4_DEMO1\", \"name\": \"C4_DEMO1\", \"selected\": true}]}";
    var dto = toObject(facilities, SelectionParametersDto.class);

    assertThat(dto.facilities).extracting("id").containsExactly("C4_DEMO1");
  }
}
