package com.elvaco.mvp.core.spi.data;

import java.util.List;

public interface Page<T> {

  int getTotalPages();

  long getTotalElements();

  <S> Page<S> map(Converter<? super T, ? extends S> converter);

  List<T> getContent();
}
