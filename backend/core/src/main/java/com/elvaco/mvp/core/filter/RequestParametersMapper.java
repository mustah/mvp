package com.elvaco.mvp.core.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.exception.InvalidId;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;

import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.spi.data.RequestParameter.ADDRESS;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ALARM;
import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.COLLECTION_AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.COLLECTION_BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.FACILITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.GATEWAY_ID;
import static com.elvaco.mvp.core.spi.data.RequestParameter.GATEWAY_SERIAL;
import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static com.elvaco.mvp.core.spi.data.RequestParameter.MANUFACTURER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.MEDIUM;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ORGANISATION;
import static com.elvaco.mvp.core.spi.data.RequestParameter.QUANTITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.Q_ADDRESS;
import static com.elvaco.mvp.core.spi.data.RequestParameter.Q_CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.Q_FACILITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.Q_ORGANISATION;
import static com.elvaco.mvp.core.spi.data.RequestParameter.Q_SECONDARY_ADDRESS;
import static com.elvaco.mvp.core.spi.data.RequestParameter.Q_SERIAL;
import static com.elvaco.mvp.core.spi.data.RequestParameter.REPORTED;
import static com.elvaco.mvp.core.spi.data.RequestParameter.REPORT_AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.REPORT_BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.RESOLUTION;
import static com.elvaco.mvp.core.spi.data.RequestParameter.SECONDARY_ADDRESS;
import static com.elvaco.mvp.core.spi.data.RequestParameter.SERIAL;
import static com.elvaco.mvp.core.spi.data.RequestParameter.SORT;
import static com.elvaco.mvp.core.spi.data.RequestParameter.THRESHOLD;
import static com.elvaco.mvp.core.spi.data.RequestParameter.THRESHOLD_AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.THRESHOLD_BEFORE;
import static java.util.stream.Collectors.toList;

@UtilityClass
public final class RequestParametersMapper {

  private static final Map<RequestParameter, Function<List<String>, VisitableFilter>>
    PARAMETER_TO_FILTER = new HashMap<>();
  private static final List<RequestParameter> IGNORED_PARAMETERS = List.of(
    THRESHOLD_BEFORE,
    THRESHOLD_AFTER,
    COLLECTION_AFTER,
    COLLECTION_BEFORE,
    REPORT_AFTER,
    REPORT_BEFORE,
    SORT,
    QUANTITY,
    RESOLUTION
  );

  static {
    PARAMETER_TO_FILTER.put(CITY, (values) -> new CityFilter(values, MatchType.EXACT));
    PARAMETER_TO_FILTER.put(GATEWAY_SERIAL, (values) -> new SerialFilter(values, MatchType.EXACT));
    PARAMETER_TO_FILTER.put(SERIAL, (values) -> new SerialFilter(values, MatchType.WILDCARD));
    PARAMETER_TO_FILTER.put(ADDRESS, (values) -> new AddressFilter(values, MatchType.EXACT));
    PARAMETER_TO_FILTER.put(MEDIUM, MediumFilter::new);
    PARAMETER_TO_FILTER.put(FACILITY, (values) -> new FacilityFilter(values, MatchType.EXACT));
    PARAMETER_TO_FILTER.put(Q_FACILITY, (values) -> new FacilityFilter(values, MatchType.WILDCARD));
    PARAMETER_TO_FILTER.put(MANUFACTURER, ManufacturerFilter::new);
    PARAMETER_TO_FILTER.put(
      SECONDARY_ADDRESS,
      (values) -> new SecondaryAddressFilter(values, MatchType.EXACT)
    );
    PARAMETER_TO_FILTER.put(
      Q_SECONDARY_ADDRESS,
      (values) -> new SecondaryAddressFilter(values, MatchType.WILDCARD)
    );
    PARAMETER_TO_FILTER.put(
      REPORTED,
      (values) -> new MeterStatusFilter(
        values.stream().map(StatusType::from).collect(toList())
      )
    );
    PARAMETER_TO_FILTER.put(
      GATEWAY_ID,
      (values) -> new GatewayIdFilter(toUuids(values))
    );
    PARAMETER_TO_FILTER.put(
      LOGICAL_METER_ID,
      (values) -> new LogicalMeterIdFilter(toUuids(values))
    );
    PARAMETER_TO_FILTER.put(
      ORGANISATION,
      (values) -> new OrganisationIdFilter(toUuids(values))
    );
    PARAMETER_TO_FILTER.put(
      RequestParameter.WILDCARD,
      WildcardFilter::new
    );
    PARAMETER_TO_FILTER.put(
      THRESHOLD,
      (values) ->
        new MeasurementThresholdFilter(values.get(0))
    );
    PARAMETER_TO_FILTER.put(ALARM, AlarmFilter::new);
    PARAMETER_TO_FILTER.put(Q_CITY, (values) -> new CityFilter(values, MatchType.WILDCARD));
    PARAMETER_TO_FILTER.put(Q_ADDRESS, (values) -> new AddressFilter(values, MatchType.WILDCARD));
    PARAMETER_TO_FILTER.put(Q_ORGANISATION, WildcardFilter::new);
  }

  public static Filters toFilters(RequestParameters requestParameters) {
    Collection<VisitableFilter> visitableFilters = new ArrayList<>();
    requestParameters.getThresholdPeriod()
      .ifPresent(period -> visitableFilters.add(
        new ThresholdPeriodFilter(List.of(period), period))
      );
    requestParameters.getCollectionPeriod()
      .ifPresent(period -> visitableFilters.add(
        new CollectionPeriodFilter(List.of(period), period))
      );
    requestParameters.getReportPeriod()
      .ifPresent(period -> visitableFilters.add(
        new ReportPeriodFilter(List.of(period), period))
      );
    visitableFilters.addAll(
      requestParameters.entrySet().stream()
        .filter(param -> !param.getValue().isEmpty() && !isIgnored(param.getKey()))
        .map(param -> parameterToFilter(param.getKey(), param.getValue()))
        .collect(toList())
    );

    var filters = new Filters(visitableFilters);

    return requestParameters.implicitParameters()
      .map(RequestParametersMapper::toFilters)
      .map(filters::add)
      .orElse(filters);
  }

  private static List<UUID> toUuids(List<String> ids) {
    try {
      return ids.stream().map(UUID::fromString).collect(toList());
    } catch (IllegalArgumentException ex) {
      throw new InvalidId(ex.getMessage());
    }
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
    if (parameter.equals(Q_SERIAL)) {
      return SERIAL;
    }
    return parameter;
  }
}
