package com.elvaco.mvp.testdata;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static com.elvaco.mvp.constants.Constants.AUTHORIZATION;
import static java.util.Collections.singletonList;

public final class RestClient {

  private String baseUrl;

  private final TestRestTemplate template;

  protected RestClient(int serverPort) {
    baseUrl = "http://localhost:" + serverPort + "/api";
    this.template = new TestRestTemplate(new RestTemplate());
  }

  public <T> ResponseEntity<T> get(String url, Class<T> clazz) {
    return template.getForEntity(apiUrlOf(url), clazz);
  }

  public <T> ResponseEntity<T> post(String url, Object request, Class<T> responseType) {
    return template.postForEntity(apiUrlOf(url), request, responseType);
  }

  public RestClient loginWith(String username, String password) {
    String authentication = username + ":" + password;
    String token = new String(Base64.getEncoder().encode(authentication.getBytes(StandardCharsets.UTF_8)));
    return authorization(token);
  }

  public RestClient logout() {
    template.getRestTemplate().setInterceptors(
      singletonList((request, body, execution) -> {
        request.getHeaders().remove(AUTHORIZATION);
        return execution.execute(request, body);
      }));
    return this;
  }

  private RestClient authorization(String token) {
    return addHeader(AUTHORIZATION, "Basic " + token);
  }

  private RestClient addHeader(String headerName, String value) {
    template.getRestTemplate().setInterceptors(
      singletonList((request, body, execution) -> {
        request.getHeaders().add(headerName, value);
        return execution.execute(request, body);
      }));
    return this;
  }

  private String apiUrlOf(String url) {
    return baseUrl + url;
  }
}
