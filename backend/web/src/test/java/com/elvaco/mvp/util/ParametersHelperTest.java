package com.elvaco.mvp.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;

import static com.elvaco.mvp.util.ParametersHelper.combineParams;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ParametersHelperTest {

  @Test
  public void throwsExceptionWhenPathVariablesAreNull() {
    assertThatThrownBy(() -> combineParams(null, null))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void throwsExceptionWhenRequestParametersAreNull() {
    assertThatThrownBy(() -> combineParams(emptyMap(), null))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void emptyInputVariableReturnsEmptyMap() {
    assertThat(combineParams(emptyMap(), emptyMap())).isEmpty();
  }

  @Test
  public void createMultiMapOfPathVariable() {
    Map<String, String> pathVars = new HashMap<>();
    pathVars.put("a", "b");
    pathVars.put("c", "d");

    assertThat(combineParams(pathVars, emptyMap())).isEqualTo(ImmutableMultimap.builder()
                                                                .put("a", "b")
                                                                .put("c", "d")
                                                                .build()
                                                                .asMap());
  }

  @Test
  public void createMultiMapOfPathVariableAndRequestParameters() {
    Map<String, String> pathVars = new HashMap<>();
    pathVars.put("a", "b");
    pathVars.put("c", "d");

    Map<String, List<String>> requestParams = new HashMap<>();
    requestParams.put("n", asList("x", "y", "z"));
    requestParams.put("x", asList("testing", "to"));

    assertThat(combineParams(pathVars, requestParams)).isEqualTo(ImmutableMultimap.builder()
                                                                   .put("a", "b")
                                                                   .put("c", "d")
                                                                   .putAll("n", "x", "y", "z")
                                                                   .putAll("x", "testing", "to")
                                                                   .build()
                                                                   .asMap());
  }
}
