package com.elvaco.mvp.testdata;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;
import com.elvaco.mvp.core.spi.data.RequestParameter;

import lombok.Builder;

import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.COLLECTION_AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.COLLECTION_BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static com.elvaco.mvp.core.spi.data.RequestParameter.Q;
import static com.elvaco.mvp.core.spi.data.RequestParameter.QUANTITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.RESOLUTION;
import static com.elvaco.mvp.core.spi.data.RequestParameter.SORT;
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

    public UrlBuilder parameter(String parameter, Collection<?> values) {
      UrlBuilder b = this;
      for (Object v : values) {
        b = b.parameter(parameter, v);
      }
      return b;
    }

    public UrlBuilder period(ZonedDateTime start, ZonedDateTime stop) {
      return parameter(BEFORE, stop).parameter(AFTER, start);
    }

    public UrlBuilder collectionPeriod(ZonedDateTime start, ZonedDateTime stop) {
      return parameter(COLLECTION_BEFORE, stop).parameter(COLLECTION_AFTER, start);
    }

    public UrlBuilder city(String city) {
      return parameter(CITY, city);
    }

    public UrlBuilder city(Location location) {
      return parameter(CITY, location.getCountryOrUnknown() + ", " + location.getCityOrUnknown());
    }

    public UrlBuilder quantity(Quantity quantity) {
      return quantity(quantity.name);
    }

    public UrlBuilder quantity(String quantityPresentation) {
      return parameter(QUANTITY, quantityPresentation);
    }

    public UrlBuilder resolution(String resolution) {
      return parameter(RESOLUTION, resolution);
    }

    public UrlBuilder resolution(TemporalResolution resolution) {
      return parameter(RESOLUTION, resolution.name());
    }

    public UrlBuilder page(long pageNum) {
      return parameter("page", pageNum);
    }

    public UrlBuilder size(long pageSize) {
      return parameter("size", pageSize);
    }

    public UrlBuilder sortBy(Object value) {
      return parameter(SORT, value);
    }

    public UrlBuilder logicalMeterId(UUID id) {
      return parameter(LOGICAL_METER_ID, id);
    }

    public UrlBuilder filter(String filter) {
      return parameter(Q, filter);
    }
  }
}
