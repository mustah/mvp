package com.elvaco.mvp.database.repository.queryfilters;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.core.spi.data.RequestParameters;

import lombok.experimental.UtilityClass;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.SortOrder;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;

import static com.elvaco.mvp.core.spi.data.RequestParameter.SORT;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class SortUtil {

  public static Sort getSortOrUnsorted(RequestParameters parameters) {
    return getSort(parameters).orElse(Sort.unsorted());
  }

  public static Collection<SortField<?>> resolveSortFields(
    RequestParameters parameters,
    Map<String, Field<?>> sortFieldsMap
  ) {
    return getSort(parameters).stream()
      .flatMap(Streamable::stream)
      .map(order -> sortFieldsMap.get(order.getProperty())
        .sort(SortOrder.valueOf(order.getDirection().name())))
      .collect(toList());
  }

  private static Optional<Sort> getSort(RequestParameters parameters) {
    return parameters.has(SORT)
      .map(p -> p.getValues(SORT).stream()
        .map(s -> new Sort.Order(getDirection(s), getProperty(s)))
        .collect(toList()))
      .map(Sort::by);
  }

  private static Sort.Direction getDirection(String s) {
    return Optional.ofNullable(s)
      .filter(sort -> sort.contains(","))
      .flatMap(sort -> Sort.Direction.fromOptionalString(s.substring(s.indexOf(",") + 1)))
      .orElse(Sort.Direction.ASC);
  }

  private static String getProperty(String s) {
    return s.contains(",") ? s.substring(0, s.indexOf(",")) : s;
  }
}
