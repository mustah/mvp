package com.elvaco.geoservice.controller;

import com.elvaco.geoservice.dto.GeoRequest;
import com.elvaco.geoservice.service.RequestQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeoController {

  private final RequestQueue requestQueue;

  @Autowired
  public GeoController(RequestQueue requestQueue) {
    this.requestQueue = requestQueue;
  }

  @GetMapping("/byAddress")
  public String requestByAddress(GeoRequest request) {
    requestQueue.enqueueRequest(request);
    return HttpStatus.OK.name();
  }

  @PostMapping("/byAddress")
  public String postRequestByAddress(@RequestBody GeoRequest request) {
    requestQueue.enqueueRequest(request);
    return HttpStatus.OK.name();
  }
}
