package com.elvaco.mvp.database.repository.mappers;

import java.util.Map;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.data.Order;
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
