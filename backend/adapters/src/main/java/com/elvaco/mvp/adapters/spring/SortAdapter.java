package com.elvaco.mvp.adapters.spring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.elvaco.mvp.core.spi.data.Order;
import com.elvaco.mvp.core.spi.data.Sort;
import org.springframework.data.domain.Sort.Direction;

import static java.util.stream.Collectors.toList;

public class SortAdapter implements Sort {
  private final List<Order> orders;

  public SortAdapter(org.springframework.data.domain.Sort sort) {
    orders = new ArrayList<>();

    if (sort != null) {
      Iterator<org.springframework.data.domain.Sort.Order> iterator = sort.iterator();
      iterator.forEachRemaining(
        springOrder -> orders.add(
          new OrderAdapter(
            springOrder.getDirection().name(),
            springOrder.getProperty()
          )
        )
      );
    }
  }

  @Override
  public List<Order> getOrders() {
    return orders;
  }

  public org.springframework.data.domain.Sort getAsSpringSort() {
    if (orders.size() > 0) {
      return new org.springframework.data.domain.Sort(
        orders.stream().map(this::mapOrderToSprintOrder).collect(toList())
      );
    } else {
      return null;
    }
  }

  private org.springframework.data.domain.Sort.Order mapOrderToSprintOrder(Order order) {
    org.springframework.data.domain.Sort.Order springOrder =
      new org.springframework.data.domain.Sort.Order(
        Direction.fromString(order.getDirection().name()),
        order.getProperty()
      );

    if (order.isIgnoreCase()) {
      return springOrder.ignoreCase();
    } else {
      return springOrder;
    }
  }
}
