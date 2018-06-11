package com.elvaco.mvp.web.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.elvaco.mvp.adapters.spring.RequestParametersAdapter.requestParametersOf;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RequestParametersTest {

  @Test
  public void isEmptyWhenCreatedWithNullMultiMap() {
    assertThat(requestParametersOf(null).isEmpty()).isTrue();
  }

  @Test
  public void throwsExceptionWhenRequestParametersAreNull() {
    assertThatThrownBy(() -> new RequestParametersAdapter().setAll(null))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void isEmpty() {
    assertThat(new RequestParametersAdapter().isEmpty()).isTrue();
  }

  @Test
  public void createAdapter() {
    RequestParametersAdapter requestParameters = new RequestParametersAdapter();

    requestParameters
      .add("a", "b")
      .add("c", "d");

    assertThat(requestParameters.multiValueMap())
      .isEqualTo(ImmutableMultimap.builder()
        .put("a", "b")
        .put("c", "d")
        .build()
        .asMap());
  }

  @Test
  public void createFromMultiValueMap() {
    MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
    multiValueMap.add("a", "b");

    RequestParametersAdapter requestParameters =
      (RequestParametersAdapter) requestParametersOf(multiValueMap);

    assertThat(requestParameters.multiValueMap())
      .isEqualTo(ImmutableMultimap.builder()
        .put("a", "b")
        .build()
        .asMap());
  }

  @Test
  public void createWithPathVariables() {
    Map<String, String> pathVariable = new HashMap<>();
    pathVariable.put("a", "b");
    pathVariable.put("c", "d");

    RequestParametersAdapter adapter = new RequestParametersAdapter();
    adapter.setAll(pathVariable);

    assertThat(adapter.multiValueMap())
      .isEqualTo(ImmutableMultimap.builder()
        .put("a", "b")
        .put("c", "d")
        .build()
        .asMap());
  }

  @Test
  public void addSameVariableWithMultipleValues() {
    RequestParametersAdapter adapter = new RequestParametersAdapter();
    adapter
      .add("a", "b")
      .add("a", "c")
      .add("a", "d");

    assertThat(adapter.getValues("a")).containsExactly("b", "c", "d");
  }

  @Test
  public void createMultiMapOfPathVariableAndRequestParameters() {
    Map<String, String> pathVariables = new HashMap<>();
    pathVariables.put("a", "b");
    pathVariables.put("c", "d");

    Map<String, List<String>> requestParams = new HashMap<>();
    requestParams.put("n", asList("x", "y", "z"));
    requestParams.put("x", asList("testing", "to"));

    RequestParametersAdapter adapter =
      (RequestParametersAdapter) requestParametersOf(requestParams)
        .setAll(pathVariables);

    assertThat(adapter.multiValueMap())
      .isEqualTo(ImmutableMultimap.builder()
        .put("a", "b")
        .put("c", "d")
        .putAll("n", "x", "y", "z")
        .putAll("x", "testing", "to")
        .build()
        .asMap());
  }

  @Test
  public void pathVarsOverridesRequestParams() {
    Map<String, String> pathVariables = new HashMap<>();
    pathVariables.put("a", "b");
    pathVariables.put("c", "d");

    Map<String, List<String>> requestParams = new HashMap<>();
    requestParams.put("c", asList("x", "y", "z"));

    RequestParametersAdapter adapter =
      (RequestParametersAdapter) requestParametersOf(requestParams)
        .setAll(pathVariables);

    assertThat(adapter.multiValueMap())
      .isEqualTo(ImmutableMultimap.builder()
        .put("a", "b")
        .put("c", "d")
        .build()
        .asMap());
  }

  @Test
  public void replaceAllValuesWith() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add("organisation", "1")
      .add("organisation", "2")
      .replace("organisation", "6");

    assertThat(parameters.getValues("organisation")).containsExactly("6");
  }

  @Test
  public void replaceEmpty() {
    RequestParameters parameters = new RequestParametersAdapter()
      .replace("organisation", "6");

    assertThat(parameters.getValues("organisation")).containsExactly("6");
  }

  @Test
  public void getNonExistingValuesShouldReturnEmptyList() {
    assertThat(new RequestParametersAdapter().getValues("nothing")).isEmpty();
  }

  @Test
  public void entrySetShouldBeEmptyWhenThereAreNoParameters() {
    assertThat(new RequestParametersAdapter().entrySet()).isEmpty();
    assertThat(requestParametersOf(null).entrySet()).isEmpty();

    Map<String, List<String>> multiValueMap = null;
    assertThat(requestParametersOf(multiValueMap).entrySet()).isEmpty();
  }

  @Test
  public void setAllIds() {
    UUID id1 = randomUUID();
    UUID id2 = randomUUID();

    RequestParameters parameters = new RequestParametersAdapter()
      .setAllIds("id", asList(id1, id2));

    assertThat(parameters.getValues("id")).containsExactly(id1.toString(), id2.toString());
  }
}
