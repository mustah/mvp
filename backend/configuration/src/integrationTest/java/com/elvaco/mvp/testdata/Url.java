package com.elvaco.mvp.testdata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import lombok.Builder;

import static java.util.Collections.emptyMap;

@Builder
public class Url implements UrlTemplate {

  private final Map<String, List<Object>> parameters;
  private final String path;

  @Override
  public String template() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, List<Object>> entry : parameters().entrySet()) {
      addParameter(sb, entry.getKey(), entry.getValue().size());
    }
    return path + sb.toString();
  }

  @Override
  public Object[] variables() {
    return parameters().values().stream()
      .flatMap(List::stream)
      .toArray();
  }

  private Map<String, List<Object>> parameters() {
    return parameters != null ? parameters : emptyMap();
  }

  private void addParameter(StringBuilder sb, String parameter, int parameterCount) {
    if (sb.length() == 0) {
      sb.append("?");
    } else {
      sb.append("&");
    }

    for (int i = 0; i < parameterCount; i++) {
      if (i > 0) {
        sb.append("&");
      }
      sb.append(parameter);
      sb.append("={");
      sb.append(parameter);
      sb.append(i);
      sb.append("}");
    }
  }

  @SuppressWarnings("unused") // This is a Lombok skeleton, it's not unused
  public static class UrlBuilder {

    public UrlBuilder parameter(RequestParameter parameter, Object value) {
      return parameter(parameter.toString(), value);
    }

    public UrlBuilder parameter(String parameter, Object value) {
      if (parameters == null) {
        parameters = new LinkedHashMap<>();
      }

      List<Object> values = parameters.getOrDefault(parameter, new ArrayList<>());
      values.add(value);
      parameters.put(parameter, values);
      return this;
    }

    public UrlBuilder sortBy(Object value) {
      return parameter(RequestParameter.SORT, value);
    }
  }
}
