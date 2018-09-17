package com.elvaco.mvp.web.dto;

import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AlarmDto {

  public Long id;
  public int mask;
  @Nullable
  public String description;

  public AlarmDto(Long id, int mask) {
    this(id, mask, null);
  }
}
