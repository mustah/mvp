package com.elvaco.geoservice;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MockedGeoService {
  private int requestCount = 0;

  @RequestMapping("/v3/json/forward/")
  public String getAddr(String addr, String country) throws IOException {
    requestCount++;
    String resource = "/notfound.json";
    if (addr.startsWith("Kabelgatan")) {
      resource = "/kabelgatan.json";
    } else if (addr.startsWith("Walls väg")) {
      resource = "/wallsväg.json";
    } else if (addr.startsWith("Fasanvägen")) {
      resource = "/fasanvägen.json";
    } else if (addr.startsWith("Drottningvägen")) {
      resource = "/växjö-drottningvägen.json";
    }
    InputStream is = MockedGeoService.class.getResourceAsStream(resource);
    return IOUtils.toString(is, Charset.forName("UTF-8"));
  }

  public int getRequestCount() {
    return requestCount;
  }

  public void clearCount() {
    requestCount = 0;
  }
}
