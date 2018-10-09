package com.elvaco.mvp.adapters.spring;

import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.Sort;

public class PageableAdapter implements Pageable {

  private final org.springframework.data.domain.Pageable delegate;

  public PageableAdapter(org.springframework.data.domain.Pageable pageable) {
    this.delegate = pageable;
  }

  @Override
  public int getPageNumber() {
    return delegate.getPageNumber();
  }

  @Override
  public int getPageSize() {
    return delegate.getPageSize();
  }

  @Override
  public long getOffset() {
    return delegate.getOffset();
  }

  @Override
  public Sort getSort() {
    return new SortAdapter(delegate.getSort());
  }

  @Override
  public Pageable next() {
    return new PageableAdapter(delegate.next());
  }

  @Override
  public Pageable previousOrFirst() {
    return new PageableAdapter(delegate.previousOrFirst());
  }

  @Override
  public Pageable first() {
    return new PageableAdapter(delegate.first());
  }

  @Override
  public boolean hasPrevious() {
    return delegate.hasPrevious();
  }
}
