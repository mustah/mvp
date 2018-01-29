package com.elvaco.mvp.core.spi.data;

public interface Converter<S, T> {
  T convert(S source);
}
