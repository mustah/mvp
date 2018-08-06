package com.elvaco.mvp.database.repository.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.Order;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import static java.util.stream.Collectors.toList;

public abstract class SortingEntityMapper {

  abstract Map<String, String> getSortingMap();

  @Nullable
  public org.springframework.data.domain.Sort getAsSpringSort(
    com.elvaco.mvp.core.spi.data.Sort sort
  ) {
    if (sort.getOrders().isEmpty()) {
      return null;
    } else {
      return new Sort(
        sort.getOrders().stream()
          .map(this::mapOrderToSprintOrder)
          .collect(toList())
      );
    }
  }

  public Optional<Sort> getAsSpringSort(RequestParameters parameters) {
    if (parameters.hasName("sort")) {
      List<Sort.Order> orders = new ArrayList<>();

      parameters.getValues("sort")
        .forEach(s -> orders.add(new Sort.Order(getDirection(s), getProperty(s))));

      return Optional.of(new org.springframework.data.domain.Sort(orders));
    }

    return Optional.empty();
  }

  private String getProperty(String s) {
    return s.contains(",") ? s.substring(0, s.indexOf(",")) : s;
  }

  private Direction getDirection(String s) {
    Direction direction = null;

    if (s.contains(",")) {
      direction = Direction.fromStringOrNull(s.substring(s.indexOf(",") + 1));
    }

    return direction == null ? Sort.DEFAULT_DIRECTION : direction;
  }

  private Sort.Order mapOrderToSprintOrder(Order order) {
    org.springframework.data.domain.Sort.Order springOrder =
      new org.springframework.data.domain.Sort.Order(
        Direction.fromString(order.getDirection().name()),
        getSortingMap().get(order.getProperty())
      );

    if (order.isIgnoreCase()) {
      return springOrder.ignoreCase();
    } else {
      return springOrder;
    }
  }
}
