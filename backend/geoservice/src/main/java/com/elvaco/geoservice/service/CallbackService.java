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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class CallbackService {

  private final CallbackRepository callbackRepository;
  private final Integer maxAttempts;

  @Autowired
  public CallbackService(
    CallbackRepository callbackRepository,
    @Value("${callback.retries:5}") Integer maxAttempts
  ) {
    this.callbackRepository = callbackRepository;
    this.maxAttempts = maxAttempts;
  }

  @Async
  public CallbackEntity enqueueCallback(URI callbackUrl, Address address, GeoLocation geo) {
    GeoResponse response = new GeoResponse(
      new AddressDto(
        address.street,
        address.zip,
        address.city,
        address.country
      ),
      new GeoDataDto(
        Double.valueOf(geo.getLongitude()),
        Double.valueOf(geo.getLatitude()),
        geo.getConfidence()
      )
    );

    CallbackEntity callback = new CallbackEntity();
    callback.setCallback(callbackUrl);
    callback.setAttempt(0);
    callback.setNextRetry(LocalDateTime.now());
    callback.setPayload(response);

    callback = callbackRepository.save(callback);
    log.info("Callback enqueued = " + callbackUrl);
    return callback;
  }

  @Async
  public CallbackEntity enqueueCallback(URI callbackUrl, Address address, ErrorDto error) {
    error.address = new AddressDto(address.street, address.zip, address.city, address.country);

    CallbackEntity callback = new CallbackEntity();
    callback.setCallback(callbackUrl);
    callback.setAttempt(0);
    callback.setNextRetry(LocalDateTime.now());
    callback.setPayload(error);

    callback = callbackRepository.save(callback);
    log.info("Error Callback enqueued = " + callbackUrl);
    return callback;
  }

  @Transactional
  public void popFromQueue(CallbackEntity callback) {
    RestTemplate template = new RestTemplate();
    try {
      log.info(
        "Trying callback id = {}, url = {}, attempt = {}",
        callback.getId(),
        callback.getCallback(),
        (callback.getAttempt() + 1)
      );

      String result = template.postForObject(
        callback.getCallback(),
        callback.getPayload(),
        String.class
      );
      log.info("Callback id = {} result = ", callback.getId(), result);
      callbackRepository.delete(callback);
    } catch (RuntimeException e) {
      log.warn("Callback id = {} failed.", callback.getId(), e);

      callback.setAttempt(callback.getAttempt() + 1);
      long nanos = 1000 * 1000 * (long) Math.pow(100, callback.getAttempt());
      callback.setNextRetry(LocalDateTime.now().plusNanos(nanos));
      callbackRepository.save(callback);

      if (callback.getAttempt() >= maxAttempts - 1) {
        callbackRepository.delete(callback);
        log.error(
          "Too many retries. Could not connect to callback id = {} at {}",
          callback.getId(),
          callback.getCallback(),
          e
        );
      }
    }
  }

  @Scheduled(fixedRate = 1000)
  public synchronized void popFromQueue() {
    callbackRepository.findByNextRetryBeforeNowOrderByNextRetryAsc().forEach(this::popFromQueue);
  }
}
