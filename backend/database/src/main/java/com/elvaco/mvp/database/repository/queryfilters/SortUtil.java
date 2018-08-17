package com.elvaco.mvp.database.repository.queryfilters;

import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class SortUtil {

  public static Optional<Sort> getSort(RequestParameters parameters) {
    return parameters.has("sort")
      .map(p -> p.getValues("sort").stream()
        .map(s -> new Sort.Order(getDirection(s), getProperty(s)))
        .collect(toList()))
      .map(Sort::new);
  }

  @Nullable
  public static Sort getSortOrNull(RequestParameters parameters) {
    return getSort(parameters).orElse(null);
  }

  private static Sort.Direction getDirection(String s) {
    return Optional.ofNullable(s)
      .filter(sort -> sort.contains(","))
      .map(sort -> Sort.Direction.fromStringOrNull(s.substring(s.indexOf(",") + 1)))
      .orElse(Sort.Direction.ASC);
  }

  private static String getProperty(String s) {
    return s.contains(",") ? s.substring(0, s.indexOf(",")) : s;
  }
}
