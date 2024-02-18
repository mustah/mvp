package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.core.spi.data.Order;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

@UtilityClass
public class SortMapper {
  public static Sort getAsSpringSort(com.elvaco.mvp.core.spi.data.Sort sort) {
    return Sort.by(sort.getOrders().stream()
      .map(order -> order.getDirection() == Order.Direction.ASC
        ? Sort.Order.asc(order.getProperty())
        : Sort.Order.desc(order.getProperty()))
      .toList());
  }
}
