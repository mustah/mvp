package com.elvaco.mvp.testdata;

import static com.elvaco.mvp.constants.Constants.AUTHORIZATION;
import static java.util.Collections.singletonList;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public final class RestClient {

  private final TestRestTemplate template;
  private String baseUrl;

  protected RestClient(int serverPort) {
    baseUrl = "http://localhost:" + serverPort + "/api";
    this.template = new TestRestTemplate(new RestTemplate());
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public <T> ResponseEntity<T> get(String url, Class<T> clazz) {
    return template.getForEntity(apiUrlOf(url), clazz);
  }

  public <T> ResponseEntity<T> post(String url, Object request, Class<T> responseType) {
    return template.postForEntity(apiUrlOf(url), request, responseType);
  }

  public <T> ResponseEntity<RestResponsePage<T>> getPage(String url, Class<T> pagedClass) {
    ParameterizedTypeReference<RestResponsePage<T>> responseType =
        new ParameterizedTypeReference<RestResponsePage<T>>() {
          public Type getType() {
            return new ParameterizedTypeReferenceImpl(
                (ParameterizedType) super.getType(), new Type[] {pagedClass});
          }
        };
    ResponseEntity<RestResponsePage<T>> r =
        template.exchange(baseUrl + url, HttpMethod.GET, null, responseType);
    return r;
  }

  public RestClient loginWith(String username, String password) {
    String authentication = username + ":" + password;
    byte[] authBytes = authentication.getBytes(StandardCharsets.UTF_8);
    String token = new String(Base64.getEncoder().encode(authBytes), StandardCharsets.UTF_8);
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
