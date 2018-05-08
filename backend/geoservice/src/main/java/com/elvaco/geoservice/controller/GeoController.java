package com.elvaco.geoservice.controller;

import javax.validation.Valid;

import com.elvaco.geoservice.dto.GeoRequest;
import com.elvaco.geoservice.service.RequestQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class GeoController {

  private final RequestQueue requestQueue;

  @GetMapping("/address")
  public ResponseEntity<String> requestByAddress(@Valid GeoRequest request) {
    requestQueue.enqueueRequest(request);
    return ResponseEntity.ok("OK");
  }
}
