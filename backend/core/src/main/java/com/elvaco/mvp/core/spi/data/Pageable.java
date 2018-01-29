package com.elvaco.mvp.core.spi.data;

public interface Pageable {
  int getPageNumber();

  int getPageSize();

  int getOffset();

  Sort getSort();

  Pageable next();

  Pageable previousOrFirst();

  Pageable first();

  boolean hasPrevious();
}
