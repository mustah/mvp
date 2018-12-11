package com.elvaco.mvp.web.api;

import com.elvaco.mvp.core.usecase.MissingMeasurementUseCases;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@RestApi("/api/v1/missing/measurement")
public class MissingMeasurementsController {

  private final MissingMeasurementUseCases missingMeasurementUseCases;

  @PostMapping("refresh")
  public ResponseEntity<Void> refresh() {
    if (missingMeasurementUseCases.refreshAsUser()) {
      return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    } else {
      return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
    }
  }
}
