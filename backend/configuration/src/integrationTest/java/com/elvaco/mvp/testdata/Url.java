package com.elvaco.mvp.testdata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import lombok.Builder;

@Builder
public class Url implements UrlTemplate {

  private Map<RequestParameter, List<Object>> parameters;
  private String path;

  @Override
  public String template() {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<RequestParameter, List<Object>> entry : parameters.entrySet()) {
      addParameter(sb, entry.getKey(), entry.getValue());
    }
    return path + sb.toString();
  }

  @Override
  public Object[] variables() {
    return parameters.values()
      .stream()
      .flatMap(List::stream)
      .toArray();
  }

  private void addParameter(StringBuilder sb, RequestParameter parameter, List<Object> values) {
    if (sb.length() == 0) {
      sb.append("?");
    } else {
      sb.append("&");
    }

    for (int i = 0; i < values.size(); i++) {
      sb.append(parameter.toString());
      sb.append("={");
      sb.append(parameter.toString());
      sb.append(i);
      sb.append("}");
    }
  }

  @SuppressWarnings("unused") // This is a Lombok skeleton, it's not unused
  public static class UrlBuilder {

    public UrlBuilder parameter(RequestParameter parameter, Object value) {
      if (parameters == null) {
        parameters = new LinkedHashMap<>();
      }
      List<Object> values = parameters.getOrDefault(parameter, new ArrayList<>());
      values.add(value);
      parameters.put(parameter, values);
      return this;
    }
  }
}
