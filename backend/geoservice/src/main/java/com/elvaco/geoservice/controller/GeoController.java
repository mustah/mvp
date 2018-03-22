package com.elvaco.geoservice.controller;

import com.elvaco.geoservice.dto.GeoRequest;
import com.elvaco.geoservice.service.RequestQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeoController {
  @Autowired
  RequestQueue requestQueue;

  @GetMapping("/byAddress")
  public String requestByAddress(GeoRequest request) {
    System.out.println(request.getCallbackUrl());
    System.out.println(request.getAddress());
    requestQueue.enqueueRequest(request);
    return "OK";
  }

  @RequestMapping(value = "/byAddress")
  @PostMapping
  public String requestByAddresspost(@RequestBody GeoRequest request) {
    System.out.println(request.getCallbackUrl());
    System.out.println(request.getAddress());
    requestQueue.enqueueRequest(request);
    return "OK";
  }
}