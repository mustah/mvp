package com.elvaco.mvp.database.repository.mappers;

import com.elvaco.mvp.adapters.spring.SortAdapter;

import org.junit.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;

public class SortMapperTest {
  @Test
  public void getAsSpringSort_unsorted() {
    testMapping(Sort.unsorted());
  }

  @Test
  public void getAsSpringSort_single() {
    testMapping(Sort.by(Sort.Order.asc("a")));
  }

  @Test
  public void getAsSpringSort_multiple() {
    testMapping(Sort.by(Sort.Order.asc("a"), Sort.Order.desc("b")));
  }

  private void testMapping(Sort sort) {
    assertThat(SortMapper.getAsSpringSort(new SortAdapter(sort)))
      .isEqualTo(sort);
  }
}
