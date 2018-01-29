package com.elvaco.mvp.spring;

import java.util.List;

import com.elvaco.mvp.core.spi.data.Converter;
import com.elvaco.mvp.core.spi.data.Page;

public class PageAdapter<T> implements Page<T> {
  private final org.springframework.data.domain.Page<T> delegate;

  public PageAdapter(org.springframework.data.domain.Page<T> page) {
    this.delegate = page;
  }

  @Override
  public int getTotalPages() {
    return delegate.getTotalPages();
  }

  @Override
  public long getTotalElements() {
    return delegate.getTotalElements();
  }

  @Override
  public <S> Page<S> map(Converter<? super T, ? extends S> converter) {
    return new PageAdapter<>(delegate.map(converter::convert));
  }

  @Override
  public List<T> getContent() {
    return delegate.getContent();
  }
}
