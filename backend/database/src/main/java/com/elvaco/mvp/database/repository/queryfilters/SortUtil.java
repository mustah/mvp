package com.elvaco.mvp.database.repository.queryfilters;

import java.util.Optional;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

import static com.elvaco.mvp.core.spi.data.RequestParameter.SORT;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class SortUtil {

  public static Optional<Sort> getSort(
    RequestParameters parameters
  ) {
    return parameters.has(SORT)
      .map(p -> p.getValues(SORT).stream()
        .map(s -> new Sort.Order(getDirection(s), getProperty(s)))
        .collect(toList()))
      .map(Sort::by);
  }

  public static Sort getSortOrUnsorted(
    RequestParameters parameters
  ) {
    return getSort(parameters).orElse(Sort.unsorted());
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
