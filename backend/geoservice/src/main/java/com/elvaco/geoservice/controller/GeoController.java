package com.elvaco.geoservice.controller;

import com.elvaco.geoservice.dto.GeoRequest;
import com.elvaco.geoservice.service.RequestQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class GeoController {

  private final RequestQueue requestQueue;

  @Autowired
  public GeoController(RequestQueue requestQueue) {
    this.requestQueue = requestQueue;
  }

  @GetMapping("/address")
  public ResponseEntity<String> requestByAddress(GeoRequest request) {
    if (request.isValid()) {
      requestQueue.enqueueRequest(request);
      return ResponseEntity.ok("OK");
    } else {
      log.warn("Request model is not valid: {}", request);
      return ResponseEntity.badRequest().build();
    }
  }
}
