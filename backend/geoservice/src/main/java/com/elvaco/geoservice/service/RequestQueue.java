package com.elvaco.geoservice.service;

import javax.transaction.Transactional;

import com.elvaco.geoservice.dto.ErrorDto;
import com.elvaco.geoservice.dto.GeoRequest;
import com.elvaco.geoservice.repository.AddressGeoRepository;
import com.elvaco.geoservice.repository.GeoRequestRepository;
import com.elvaco.geoservice.repository.entity.Address;
import com.elvaco.geoservice.repository.entity.AddressGeoEntity;
import com.elvaco.geoservice.repository.entity.GeoLocation;
import com.elvaco.geoservice.repository.entity.GeoRequestEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RequestQueue {

  @Autowired
  AddressToGeoService addressToGeoService;
  @Autowired
  CallbackService callbackService;
  @Autowired
  GeoRequestRepository requestRepo;

  @Autowired
  AddressGeoRepository addressGeoEntityRepository;

  @Transactional
  public void enqueueRequest(GeoRequest request) {
    Address addr = new Address();
    addr.setStreet(request.getAddress().getStreet());
    addr.setCity(request.getAddress().getCity());
    addr.setCountry(request.getAddress().getCountry());
    AddressGeoEntity result = addressGeoEntityRepository.findByAddress(addr);
    if (result != null) {
      System.out.println("Found in database...");
      callbackService.enqueueCallback(request.getCallbackUrl(), result.getAddress(),
          result.getGeoLocation());
      return;
    }
    GeoRequestEntity entity = new GeoRequestEntity();
    entity.setCallbackUrl(request.getCallbackUrl());
    entity.setErrorCallbackUrl(request.getErrorCallbackUrl());

    entity.setAddress(addr);
    requestRepo.save(entity);
    System.out.println("Request was enqued");
  }

  @Scheduled(fixedRate = 1000)
  public void popFromQueue() {

    Integer quota = addressToGeoService.getQuota();
    Integer maxRate = addressToGeoService.getMaxRate();

    Integer numberOfItems = Math.min(quota, maxRate);
    if (numberOfItems <= 0) {
      return;
    }
    Pageable page = new PageRequest(0, numberOfItems);
    Page<GeoRequestEntity> result = requestRepo.findByOrderByCreatedAsc(page);

    result.forEach((e) -> {

      System.out.println("Fetching GEO for: " + e.getAddress());
      //Check database to be sure whe have not found this address while enqueued
      AddressGeoEntity found = addressGeoEntityRepository.findByAddress(e.getAddress());
      if (found != null) {
        callbackService.enqueueCallback(e.getCallbackUrl(), e.getAddress(), found.getGeoLocation());
      } else {
        GeoLocation geoLocation = addressToGeoService.getGeoByAddress(e.getAddress());
        if (geoLocation != null) {
          AddressGeoEntity entity = new AddressGeoEntity();
          entity.setAddress(e.getAddress());

          entity.setGeoLocation(geoLocation);

          addressGeoEntityRepository.save(entity);
          callbackService.enqueueCallback(e.getCallbackUrl(), e.getAddress(), geoLocation);
        } else {
          callbackService.enqueueCallback(e.getErrorCallbackUrl(), e.getAddress(),
              new ErrorDto().setErrorCode(1).setMessage("No geolocation found"));
        }
      }
      requestRepo.delete(e);
    });
  }

}
