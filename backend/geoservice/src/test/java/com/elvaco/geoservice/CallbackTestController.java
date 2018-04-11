package com.elvaco.geoservice;

import com.elvaco.geoservice.dto.ErrorDto;
import com.elvaco.geoservice.dto.GeoResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CallbackTestController {

  private volatile Object lastResponse;

  @RequestMapping(path = "/callback", method = RequestMethod.POST)
  public String callback(@RequestBody GeoResponse response) {
    this.lastResponse = response;
    return "OK";
  }

  @RequestMapping(path = "/error", method = RequestMethod.POST)
  public String callback(@RequestBody ErrorDto response) {
    this.lastResponse = response;
    return "OK";
  }

  public Object getLastResponse() {
    return this.lastResponse;
  }

  public void setLastResponse(GeoResponse lastResponse) {
    this.lastResponse = lastResponse;
  }
}
