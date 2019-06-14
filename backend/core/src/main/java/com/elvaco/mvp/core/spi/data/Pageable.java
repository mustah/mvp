package com.elvaco.mvp.core.spi.data;

public interface Pageable {
  int getPageNumber();

  int getPageSize();

  long getOffset();

  Sort getSort();
}
