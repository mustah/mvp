package com.elvaco.mvp.web.dto;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.TemporalResolution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MeasurementRequestDto {

  public List<UUID> logicalMeterId;
  public ZonedDateTime reportAfter;
  public ZonedDateTime reportBefore;
  public Set<String> quantity;
  public TemporalResolution resolution;

  @Nullable
  public String label;
}
