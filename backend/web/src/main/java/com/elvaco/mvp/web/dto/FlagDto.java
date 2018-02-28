package com.elvaco.mvp.web.dto;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class FlagDto {

  public String title;

  @Nullable
  public String start;

  @Nullable
  public String stop;
}
