package com.elvaco.mvp.adapters.spring;

import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.Sort;

public class PageableLimit implements Pageable {

  private final org.springframework.data.domain.Pageable delegate;
  private final int limit;

  public PageableLimit(org.springframework.data.domain.Pageable pageable, int limit) {
    this.delegate = pageable;
    this.limit = limit;
  }

  @Override
  public int getPageNumber() {
    return 0;
  }

  @Override
  public int getPageSize() {
    return limit;
  }

  @Override
  public long getOffset() {
    return delegate.getOffset();
  }

  @Override
  public Sort getSort() {
    return new SortAdapter(delegate.getSort());
  }
}
