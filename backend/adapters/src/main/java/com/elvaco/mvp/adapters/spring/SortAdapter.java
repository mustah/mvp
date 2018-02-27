package com.elvaco.mvp.adapters.spring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.elvaco.mvp.core.spi.data.Order;
import com.elvaco.mvp.core.spi.data.Sort;

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
}
