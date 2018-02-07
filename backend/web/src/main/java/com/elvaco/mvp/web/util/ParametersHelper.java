package com.elvaco.mvp.web.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

public final class ParametersHelper {

  private ParametersHelper() {}

  public static Map<String, List<String>> combineParams(
    Map<String, String> pathVars,
    Map<String, List<String>> requestParams
  ) {
    Map<String, List<String>> parameters = new HashMap<>();
    parameters.putAll(requestParams);
    parameters.putAll(
      pathVars
        .entrySet()
        .stream()
        .map(e -> new SimpleEntry<>(e.getKey(), singletonList(e.getValue())))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
    );
    return parameters;
  }
}
