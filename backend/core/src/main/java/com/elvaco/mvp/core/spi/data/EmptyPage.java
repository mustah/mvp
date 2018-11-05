package com.elvaco.mvp.core.spi.data;

import java.util.List;

import static java.util.Collections.emptyList;

public class EmptyPage<T> implements Page<T> {

  public static final EmptyPage EMPTY_PAGE = new EmptyPage<>();

  private EmptyPage() { }

  @Override
  public int getTotalPages() {
    return 0;
  }

  @Override
  public long getTotalElements() {
    return 0;
  }

  @Override
  public <S> Page<S> map(Converter<? super T, ? extends S> converter) {
    return new EmptyPage<>();
  }

  @Override
  public List<T> getContent() {
    return emptyList();
  }
}
