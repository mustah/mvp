package com.elvaco.mvp.database.repository.queryfilters;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.jooq.Routines;

import lombok.experimental.UtilityClass;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.SortOrder;
import org.jooq.TableField;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;

import static com.elvaco.mvp.core.spi.data.RequestParameter.SORT;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.jooq.impl.DSL.inline;

@UtilityClass
public class SortUtil {

  public static Sort getSortOrUnsorted(RequestParameters parameters) {
    return getSort(parameters).orElse(Sort.unsorted());
  }

  public static Collection<SortField<?>> resolveSortFields(
    RequestParameters parameters,
    Map<String, Field<?>> sortFieldsMap,
    SortField<?> defaultSort
  ) {
    Collection<SortField<?>> sortFields = resolveSortFields(parameters, sortFieldsMap);
    if (sortFields.isEmpty()) {
      return Set.of(defaultSort);
    }
    return sortFields;
  }

  public static Collection<SortField<?>> resolveSortFields(
    RequestParameters parameters,
    Map<String, Field<?>> sortFieldsMap
  ) {
    return getSort(parameters)
      .stream()
      .flatMap(Streamable::stream)
      .filter(order -> sortFieldsMap.containsKey(order.getProperty()))
      .map(order -> sortFieldsMap.get(order.getProperty())
        .sort(SortOrder.valueOf(order.getDirection().name()))
      )
      .collect(toUnmodifiableList());
  }

  public static Field<Integer> levenshtein(TableField<?, String> tableField, String inlineStr) {
    return Routines.levenshtein1(
      tableField,
      inline(inlineStr, String.class)
    ).as("edit_distance");
  }

  private static Optional<Sort> getSort(RequestParameters parameters) {
    return parameters.has(SORT)
      .map(p -> p.getValues(SORT).stream()
        .filter(s -> !s.isEmpty())
        .map(s -> new Sort.Order(getDirection(s), getProperty(s)))
        .toList())
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
