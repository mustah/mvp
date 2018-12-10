package com.elvaco.mvp.database.repository.mappers;

import java.util.Map;

import com.elvaco.mvp.core.spi.data.Order;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.querydsl.QSort;

import static java.util.stream.Collectors.toList;

public abstract class SortingEntityMapper {

  abstract Map<String, ComparableExpressionBase<?>> getSortingMap();

  public QSort getAsQSort(com.elvaco.mvp.core.spi.data.Sort sort) {
    if (sort.getOrders().isEmpty()) {
      return QSort.unsorted();
    } else {

      return new QSort(sort.getOrders().stream().map(this::mapOrder).collect(toList()));
    }
  }

  private OrderSpecifier<?> mapOrder(Order order) {
    ComparableExpressionBase<?> property = getSortingMap().get(order.getProperty());
    if (Direction.fromString(order.getDirection().name()).isAscending()) {
      return property.asc();
    } else {
      return property.desc();
    }
  }
}
