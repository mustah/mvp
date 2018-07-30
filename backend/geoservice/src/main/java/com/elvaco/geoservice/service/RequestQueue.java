package com.elvaco.geoservice.service;

import javax.transaction.Transactional;

import com.elvaco.geoservice.dto.ErrorDto;
import com.elvaco.geoservice.dto.GeoRequest;
import com.elvaco.geoservice.repository.AddressGeoRepository;
import com.elvaco.geoservice.repository.GeoRequestRepository;
import com.elvaco.geoservice.repository.entity.Address;
import com.elvaco.geoservice.repository.entity.AddressGeoEntity;
import com.elvaco.geoservice.repository.entity.CallbackEntity;
import com.elvaco.geoservice.repository.entity.GeoLocation;
import com.elvaco.geoservice.repository.entity.GeoRequestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RequestQueue {

  private final AddressToGeoService addressToGeoService;
  private final CallbackService callbackService;
  private final GeoRequestRepository requestRepo;
  private final AddressGeoRepository addressGeoEntityRepository;

  @Autowired
  public RequestQueue(
    AddressToGeoService addressToGeoService,
    CallbackService callbackService,
    GeoRequestRepository requestRepo,
    AddressGeoRepository addressGeoEntityRepository
  ) {
    this.addressToGeoService = addressToGeoService;
    this.callbackService = callbackService;
    this.requestRepo = requestRepo;
    this.addressGeoEntityRepository = addressGeoEntityRepository;
  }

  @Transactional
  public void enqueueRequest(GeoRequest request) {
    Address address = new Address(
      request.getStreet(),
      request.getCity(),
      request.getCountry()
    );
    AddressGeoEntity result = addressGeoEntityRepository.findByAddress(address);
    if (!request.isForce() && result != null) {
      CallbackEntity callback = callbackService.enqueueCallback(
        request.getCallbackUrl(),
        result.getAddress(),
        result.getGeoLocation()
      );
      log.info(
        "Found in database. Enqueue result immediately, with callback id = {}",
        callback.getId()
      );
    } else {
      GeoRequestEntity entity = new GeoRequestEntity();
      entity.setCallbackUrl(request.getCallbackUrl());
      entity.setErrorCallbackUrl(request.getErrorCallbackUrl());
      entity.setAddress(address);
      entity.setForce(request.isForce());
      entity = requestRepo.save(entity);
      log.info("Request was enqueued with callback id = {}", entity.getId());
    }
  }

  @Scheduled(fixedRate = 1000)
  public synchronized void popFromQueue() {
    Integer numberOfItems = numberOfItems();
    if (numberOfItems <= 0) {
      return;
    }

    requestRepo.findByOrderByCreatedAsc(new PageRequest(0, numberOfItems))
      .forEach(request -> {
        AddressGeoEntity found = addressGeoEntityRepository.findByAddress(request.getAddress());
        if (!request.isForce() && found != null) {
          CallbackEntity callback = callbackService.enqueueCallback(
            request.getCallbackUrl(),
            request.getAddress(),
            found.getGeoLocation()
          );
          log.info("Found in database. Enqueue result. Callback id = {}", callback.getId());
        } else {
          GeoLocation geoLocation = addressToGeoService.getGeoByAddress(request.getAddress());
          if (geoLocation != null) {
            AddressGeoEntity entity =
              addressGeoEntityRepository.findByAddress(request.getAddress());
            if (entity != null) {
              entity.setGeoLocation(geoLocation);
            } else {
              entity = new AddressGeoEntity(request.getAddress(), geoLocation);
            }
            addressGeoEntityRepository.save(entity);
            CallbackEntity callback = callbackService.enqueueCallback(
              request.getCallbackUrl(),
              request.getAddress(),
              geoLocation
            );
            log.info(
              "Geolocation found, result is enqueued with callback id = {}",
              callback.getId()
            );
          } else {
            CallbackEntity callback = callbackService.enqueueCallback(
              request.getErrorCallbackUrl(),
              request.getAddress(),
              new ErrorDto(1, "No geolocation found", null)
            );
            log.warn(
              "No geo location found for '{}' with callback id = {}",
              request.getAddress(),
              callback.getId()
            );
          }
        }
        requestRepo.delete(request);
      });
  }

  private Integer numberOfItems() {
    return Math.min(addressToGeoService.getQuota(), addressToGeoService.getMaxRate());
  }
}
