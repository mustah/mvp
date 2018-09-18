package com.elvaco.mvp.web.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class SyncRequestResponseDto {

  public UUID meterId;
  public String jobId;
}
