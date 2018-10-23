package com.elvaco.mvp.core.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.domainmodels.StatusType;
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
import static com.elvaco.mvp.core.spi.data.RequestParameter.REPORTED;
import static com.elvaco.mvp.core.spi.data.RequestParameter.SERIAL;
import static com.elvaco.mvp.core.spi.data.RequestParameter.SORT;

@UtilityClass
public final class RequestParametersConverter {

  private static Map<RequestParameter, BiFunction<List<String>, SelectionPeriod, VisitableFilter>>
    periodParameterToFilterFunctionMap = new HashMap<>();

  private static Map<RequestParameter, Function<List<String>, VisitableFilter>>
    parameterToFilterFunctionMap = new HashMap<>();
  private static List<RequestParameter> ignoredParameters = List.of(BEFORE, AFTER, SORT);

  static {
    periodParameterToFilterFunctionMap.put(
      REPORTED,
      (values, period) ->
        new StatusTypeSelectionPeriodFilter(
          values.stream()
            .map(StatusType::from)
            .collect(Collectors.toList()),
          EQUAL,
          period
        )
    );

    parameterToFilterFunctionMap.put(
      CITY,
      (values) -> new CityFilter(
        values,
        EQUAL
      )
    );

    parameterToFilterFunctionMap.put(
      GATEWAY_SERIAL,
      (values) -> new SerialFilter(
        values,
        EQUAL
      )
    );

    parameterToFilterFunctionMap.put(
      SERIAL,
      (values) -> new SerialFilter(
        values,
        ComparisonMode.WILDCARD
      )
    );

    parameterToFilterFunctionMap.put(
      ADDRESS,
      (values) -> new AddressFilter(
        values,
        EQUAL
      )
    );

    parameterToFilterFunctionMap.put(
      GATEWAY_ID,
      (values) -> new GatewayIdFilter(
        values.stream().map(UUID::fromString).collect(Collectors.toList()),
        EQUAL
      )
    );

    parameterToFilterFunctionMap.put(
      ORGANISATION,
      (values) -> new OrganisationIdFilter(
        values.stream().map(UUID::fromString).collect(Collectors.toList()),
        EQUAL
      )
    );

    parameterToFilterFunctionMap.put(
      RequestParameter.WILDCARD, (values) -> new WildcardFilter(values, ComparisonMode.WILDCARD)
    );

    parameterToFilterFunctionMap.put(
      ALARM, (values) -> new AlarmFilter(values, EQUAL)
    );
  }

  public static FilterSet toFilterSet(RequestParameters requestParameters) {
    Collection<VisitableFilter> filters = new ArrayList<>();
    Optional<SelectionPeriod> selectionPeriod = requestParameters.getPeriod();
    selectionPeriod.ifPresent(selectionPeriod1 -> filters.add(new PeriodFilter(
      Collections.singletonList(selectionPeriod1),
      EQUAL,
      selectionPeriod1
    )));
    for (Map.Entry<RequestParameter, List<String>> propertyFilter : requestParameters.entrySet()) {
      List<String> values = propertyFilter.getValue();
      RequestParameter parameter = propertyFilter.getKey();
      if (!values.isEmpty() && !isIgnored(parameter)) {
        VisitableFilter filter = parameterToFilter(parameter, values)
          .orElseGet(() -> {
              selectionPeriod.ifPresent(selectionPeriod1 -> parameterToFilter(
                parameter,
                selectionPeriod1,
                values
              ));
              throw new IllegalArgumentException(
                "Parameter '" + parameter.toString() + "' not convertable to filter"
              );
            }
          );
        filters.add(filter);
      }
    }
    return new FilterSet(filters);
  }

  private static boolean isIgnored(RequestParameter parameter) {
    return ignoredParameters.contains(parameter);
  }

  private static Optional<VisitableFilter> parameterToFilter(
    RequestParameter parameter,
    SelectionPeriod period,
    List<String> values
  ) {
    parameter = disambiguateParameter(parameter);
    if (!periodParameterToFilterFunctionMap.containsKey(parameter)) {
      return Optional.empty();
    }
    return Optional.of(periodParameterToFilterFunctionMap.get(parameter)
      .apply(values, period));
  }

  private static Optional<VisitableFilter> parameterToFilter(
    RequestParameter parameter,
    List<String> values
  ) {
    parameter = disambiguateParameter(parameter);
    if (!parameterToFilterFunctionMap.containsKey(parameter)) {
      return Optional.empty();
    }
    return Optional.of(parameterToFilterFunctionMap.get(parameter)
      .apply(values));
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
