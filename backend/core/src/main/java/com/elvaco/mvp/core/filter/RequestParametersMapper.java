package com.elvaco.mvp.core.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.filter.ComparisonMode.EQUAL;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ADDRESS;
import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ALARM;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.GATEWAY_ID;
import static com.elvaco.mvp.core.spi.data.RequestParameter.GATEWAY_SERIAL;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ID;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ORGANISATION;
import static com.elvaco.mvp.core.spi.data.RequestParameter.Q_SERIAL;
import static com.elvaco.mvp.core.spi.data.RequestParameter.SERIAL;
import static com.elvaco.mvp.core.spi.data.RequestParameter.SORT;
import static java.util.stream.Collectors.toList;

@UtilityClass
public final class RequestParametersMapper {

  private static final Map<RequestParameter, Function<List<String>, VisitableFilter>>
    PARAMETER_TO_FILTER = new HashMap<>();
  private static final List<RequestParameter> IGNORED_PARAMETERS = List.of(BEFORE, AFTER, SORT);

  static {
    PARAMETER_TO_FILTER.put(CITY, (values) -> new CityFilter(values, EQUAL));
    PARAMETER_TO_FILTER.put(GATEWAY_SERIAL, (values) -> new SerialFilter(values, EQUAL));
    PARAMETER_TO_FILTER.put(SERIAL, (values) -> new SerialFilter(values, ComparisonMode.WILDCARD));
    PARAMETER_TO_FILTER.put(ADDRESS, (values) -> new AddressFilter(values, EQUAL));
    PARAMETER_TO_FILTER.put(
      GATEWAY_ID,
      (values) -> new GatewayIdFilter(
        values.stream().map(UUID::fromString).collect(toList()),
        EQUAL
      )
    );
    PARAMETER_TO_FILTER.put(
      ORGANISATION,
      (values) -> new OrganisationIdFilter(
        values.stream().map(UUID::fromString).collect(toList()),
        EQUAL
      )
    );
    PARAMETER_TO_FILTER.put(
      RequestParameter.WILDCARD,
      (values) -> new WildcardFilter(values, ComparisonMode.WILDCARD)
    );
    PARAMETER_TO_FILTER.put(ALARM, (values) -> new AlarmFilter(values, EQUAL));
  }

  public static Filters toFilters(RequestParameters requestParameters) {
    Collection<VisitableFilter> filters = new ArrayList<>();
    Optional<SelectionPeriod> selectionPeriod = requestParameters.getPeriod();
    selectionPeriod.ifPresent(selectionPeriod1 -> filters.add(new PeriodFilter(
      Collections.singletonList(selectionPeriod1),
      EQUAL,
      selectionPeriod1
    )));

    filters.addAll(
      requestParameters.entrySet()
        .stream()
        .filter(rp -> !rp.getValue().isEmpty() && !isIgnored(rp.getKey()))
        .map(rp -> parameterToFilter(
          rp.getKey(),
          rp.getValue()
        )).collect(toList())
    );
    return new Filters(filters);
  }

  private static boolean isIgnored(RequestParameter parameter) {
    return IGNORED_PARAMETERS.contains(parameter);
  }

  private static VisitableFilter parameterToFilter(
    RequestParameter parameter,
    List<String> values
  ) {
    parameter = disambiguateParameter(parameter);
    if (!PARAMETER_TO_FILTER.containsKey(parameter)) {
      throw new IllegalArgumentException(
        "Parameter '" + parameter.toString() + "' not convertible to filter"
      );
    }
    return PARAMETER_TO_FILTER.get(parameter).apply(values);
  }

  private static RequestParameter disambiguateParameter(RequestParameter parameter) {
    if (parameter.equals(ID)) {
      return GATEWAY_ID;
    }
    if (parameter.equals(Q_SERIAL)) {
      return SERIAL;
    }
    return parameter;
  }
}
