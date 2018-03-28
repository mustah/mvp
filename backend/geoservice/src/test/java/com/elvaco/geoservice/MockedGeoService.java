package com.elvaco.geoservice;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockedGeoService {

  @RequestMapping("/v3/json/forward/")
  public String getAddr(String addr) throws IOException {
    String resource = "/notfound.json";
    if (addr.startsWith("Kabelgatan")) {
      resource = "/kabelgatan.json";
    } else if (addr.startsWith("Walls väg")) {
      resource = "/wallsväg.json";
    } else if (addr.startsWith("Fasanvägen")) {
      resource = "/fasanvägen.json";
    }
    InputStream is = MockedGeoService.class.getResourceAsStream(resource);
    return IOUtils.toString(is, Charset.forName("UTF-8"));
  }

}
