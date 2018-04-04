package com.elvaco.geoservice.service;

import java.net.URI;
import java.time.LocalDateTime;
import javax.transaction.Transactional;

import com.elvaco.geoservice.dto.AddressDto;
import com.elvaco.geoservice.dto.ErrorDto;
import com.elvaco.geoservice.dto.GeoDataDto;
import com.elvaco.geoservice.dto.GeoResponse;
import com.elvaco.geoservice.repository.CallbackRepository;
import com.elvaco.geoservice.repository.entity.Address;
import com.elvaco.geoservice.repository.entity.CallbackEntity;
import com.elvaco.geoservice.repository.entity.GeoLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CallbackService {
  private final Logger logger = LoggerFactory.getLogger(CallbackService.class);

  @Autowired
  CallbackRepository callbackRepository;
  @Value("${callback.retries:5}")
  private Integer maxAttempts;

  @Async
  public CallbackEntity enqueueCallback(URI callbackUrl, Address address, GeoLocation geo) {
    // TODO: Add to callback queue
    GeoResponse response = new GeoResponse();
    AddressDto addr = new AddressDto();
    addr.setStreet(address.getStreet());
    addr.setCity(address.getCity());
    addr.setCountry(address.getCountry());
    response.setAddress(addr);
    GeoDataDto g = new GeoDataDto(
      Double.valueOf(geo.getLongitude()),
      Double.valueOf(geo.getLatitude()),
      geo.getConfidence()
    );

    response.setGeoData(g);

    CallbackEntity callback = new CallbackEntity();
    callback.setCallback(callbackUrl);
    callback.setAttempt(0);
    callback.setNextRetry(LocalDateTime.now());
    callback.setPayload(response);

    callback = callbackRepository.save(callback);
    logger.info("Callback enqueued = " + callbackUrl);
    return callback;
  }

  @Async
  public CallbackEntity enqueueCallback(URI callbackUrl, Address address, ErrorDto error) {
    AddressDto addr = new AddressDto();
    addr.setStreet(address.getStreet());
    addr.setCity(address.getCity());
    addr.setCountry(address.getCountry());
    error.setAddress(addr);

    CallbackEntity callback = new CallbackEntity();
    callback.setCallback(callbackUrl);
    callback.setAttempt(0);
    callback.setNextRetry(LocalDateTime.now());
    callback.setPayload(error);

    callback = callbackRepository.save(callback);
    logger.info("Error Callback enqueued = " + callbackUrl);
    return callback;
  }

  @Transactional
  public void popFromQueue(CallbackEntity callback) {
    RestTemplate template = new RestTemplate();

    try {
      logger.info("Trying callback id = " + callback.getId() + " url = " + callback.getCallback()
                  + ", attempt = " + (callback.getAttempt() + 1));

      String result = template.postForObject(callback.getCallback(), callback.getPayload(),
                                             String.class
      );
      logger.info("Callback id = " + callback.getId() + " result = " + result);
      callbackRepository.delete(callback);
    } catch (RuntimeException e) {
      logger.warn("Callback id = " + callback.getId() + " failed.", e);

      callback.setAttempt(callback.getAttempt() + 1);
      callback.setNextRetry(LocalDateTime.now()
                              .plusNanos(1000 * 1000 * (long) java.lang.Math.pow(
                                1 * 100,
                                callback.getAttempt()
                              )));
      callbackRepository.save(callback);
      if (callback.getAttempt() >= maxAttempts - 1) {
        callbackRepository.delete(callback);
        logger.error("To many retries. Bailing out... could not connect to callback id = "
                     + callback.getId() + " at " + callback.getCallback(), e);
      }
    }
  }

  @Scheduled(fixedRate = 1000)
  public void popFromQueue() {
    callbackRepository.findByNextRetryBeforeNowOrderByNextRetryAsc().forEach(this::popFromQueue);
  }
}
