package com.elvaco.geoservice.service;

import java.net.URI;
import java.util.Date;

import javax.transaction.Transactional;

import com.elvaco.geoservice.dto.AddressDto;
import com.elvaco.geoservice.dto.ErrorDto;
import com.elvaco.geoservice.dto.GeoDataDto;
import com.elvaco.geoservice.dto.GeoResponse;
import com.elvaco.geoservice.repository.CallbackRepository;
import com.elvaco.geoservice.repository.entity.Address;
import com.elvaco.geoservice.repository.entity.CallbackEntity;
import com.elvaco.geoservice.repository.entity.GeoLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CallbackService {
  @Autowired
  CallbackRepository callbackRepository;
  @Value("${callback.retries:5}")
  private Integer maxAttempts;

  @Async
  public void enqueueCallback(URI callbackUrl, Address address, GeoLocation geo) {
    // TODO: Add to callback queue
    GeoResponse response = new GeoResponse();
    AddressDto addr = new AddressDto();
    addr.setStreet(address.getStreet());
    addr.setCity(address.getCity());
    addr.setCountry(address.getCountry());
    response.setAddress(addr);
    GeoDataDto g = new GeoDataDto();
    g.setConfidence(geo.getConfidence());
    g.setLatitude(geo.getLatitude());
    g.setLongitude(geo.getLongitude());

    response.setGeoData(g);

    CallbackEntity callback = new CallbackEntity();
    callback.setCallback(callbackUrl);
    callback.setAttempt(0);
    callback.setNextRetry(new Date());
    callback.setPayload(response);

    callbackRepository.save(callback);
    System.out.println("Callback enqueued = " + callback);

  }

  @Async
  public void enqueueCallback(URI callbackUrl, Address address, ErrorDto error) {
    AddressDto addr = new AddressDto();
    addr.setStreet(address.getStreet());
    addr.setCity(address.getCity());
    addr.setCountry(address.getCountry());
    error.setAddress(addr);

    CallbackEntity callback = new CallbackEntity();
    callback.setCallback(callbackUrl);
    callback.setAttempt(0);
    callback.setNextRetry(new Date());
    callback.setPayload(error);

    callbackRepository.save(callback);
    System.out.println("Callback enqueued = " + callback);

  }

  @Transactional
  public void popFromQueue(CallbackEntity callback) {

    RestTemplate template = new RestTemplate();

    try {
      System.out.println("Trying callback, attempt = " + callback.getAttempt() + 1);
      ;
      String result = template.postForObject(callback.getCallback(), callback.getPayload(),
          String.class);
      System.out.println("Callback result = " + result);
      callbackRepository.delete(callback);
    } catch (RuntimeException e) {
      System.out.println("callback failed:" + e.getMessage());

      callback.setAttempt(callback.getAttempt() + 1);
      callback.setNextRetry(new Date(
          System.currentTimeMillis() + (long) java.lang.Math.pow(1 * 100, callback.getAttempt())));
      callbackRepository.save(callback);
      if (callback.getAttempt() >= maxAttempts - 1) {
        callbackRepository.delete(callback);
        System.out.println(
            "Bailing out... could not connect to callback at" + callback.getCallback().toString());
      }
    }

  }

  @Scheduled(fixedRate = 1000)
  public void popFromQueue() {

    callbackRepository.findByNextRetryBeforeNowOrderByNextRetryAsc().forEach((callback) -> {
      popFromQueue(callback);
    });
  }
}
