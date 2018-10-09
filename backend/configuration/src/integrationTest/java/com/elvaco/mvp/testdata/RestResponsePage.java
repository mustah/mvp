package com.elvaco.mvp.testdata;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@JsonIgnoreProperties({"pageable", "sort"})
public class RestResponsePage<T> extends PageImpl<T> {

  private static final long serialVersionUID = 7024191353049604570L;

  private int number;
  private int size;
  private int totalPages;
  private int numberOfElements;
  private long totalElements;
  private boolean previousPage;
  private boolean firstPage;
  private boolean nextPage;
  private boolean lastPage;
  private List<T> content;
  private Sort sort;

  public RestResponsePage() {
    super(new ArrayList<>());
  }

  @Override
  public int getNumber() {
    return number;
  }

  @Override
  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  @Override
  public int getTotalPages() {
    return totalPages;
  }

  @Override
  public int getNumberOfElements() {
    return numberOfElements;
  }

  @Override
  public long getTotalElements() {
    return totalElements;
  }

  @Override
  public List<T> getContent() {
    return content;
  }

  @Override
  public Sort getSort() {
    return sort;
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  Page<T> newPage() {
    return new PageImpl<>(
      getContent(),
      size == 0 ? Pageable.unpaged() : PageRequest.of(getNumber(), getSize(), getSort()),
      getTotalElements()
    );
  }
}
