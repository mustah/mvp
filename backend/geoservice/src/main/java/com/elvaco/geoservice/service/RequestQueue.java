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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RequestQueue {
  private final Logger logger = LoggerFactory.getLogger(RequestQueue.class);
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

      CallbackEntity callback = callbackService.enqueueCallback(request.getCallbackUrl(),
          result.getAddress(), result.getGeoLocation());
      logger.info(
          "Found in database. Enqueue result imediatly. CallbackEntity id = " + callback.getId());
      return;
    }
    GeoRequestEntity entity = new GeoRequestEntity();
    entity.setCallbackUrl(request.getCallbackUrl());
    entity.setErrorCallbackUrl(request.getErrorCallbackUrl());

    entity.setAddress(addr);
    entity = requestRepo.save(entity);
    logger.info("Request was enqued with id = " + entity.getId());
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

      logger.info("Fetching GEO for request id " + e.getId() + ": " + e.getAddress());
      // Check database to be sure we have not found this address while enqueued
      AddressGeoEntity found = addressGeoEntityRepository.findByAddress(e.getAddress());
      if (found != null) {
        CallbackEntity callback = callbackService.enqueueCallback(e.getCallbackUrl(),
            e.getAddress(), found.getGeoLocation());
        logger.info("Found in database. Enqueue result. CallbackEntity id = " + callback.getId());
      } else {
        GeoLocation geoLocation = addressToGeoService.getGeoByAddress(e.getAddress());
        if (geoLocation != null) {
          AddressGeoEntity entity = new AddressGeoEntity();
          entity.setAddress(e.getAddress());

          entity.setGeoLocation(geoLocation);

          addressGeoEntityRepository.save(entity);
          CallbackEntity callback = callbackService.enqueueCallback(e.getCallbackUrl(),
              e.getAddress(), geoLocation);
          logger.info(
              "Geolocation found, result is enqueued with callbackEntity id = " + callback.getId());
        } else {
          CallbackEntity callback = callbackService.enqueueCallback(e.getErrorCallbackUrl(),
              e.getAddress(), new ErrorDto().setErrorCode(1).setMessage("No geolocation found"));
          logger.warn("No geo location found for " + e.getAddress() + " callbackEntity id = "
              + callback.getId());
        }
      }
      requestRepo.delete(e);
    });
  }

}
