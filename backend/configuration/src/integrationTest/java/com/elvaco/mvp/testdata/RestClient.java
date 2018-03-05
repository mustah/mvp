package com.elvaco.mvp.testdata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import com.elvaco.mvp.web.dto.UserTokenDto;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static com.elvaco.mvp.web.util.Constants.API_V1;
import static com.elvaco.mvp.web.util.Constants.AUTHORIZATION;
import static com.elvaco.mvp.web.util.Constants.BASIC;
import static com.elvaco.mvp.web.util.Constants.BEARER;
import static java.util.Collections.singletonList;

public final class RestClient {

  private final TestRestTemplate template;
  private final String baseUrl;

  RestClient(int serverPort) {
    this.baseUrl = "http://localhost:" + serverPort + API_V1;
    this.template = new TestRestTemplate(new RestTemplate());
  }

  public static String apiPathOf(String url) {
    return API_V1 + url;
  }

  public <T> ResponseEntity<T> get(String url, Class<T> clazz) {
    return template.getForEntity(apiUrlOf(url), clazz);
  }

  public <T> ResponseEntity<T> post(String url, Object request, Class<T> responseType) {
    return template.postForEntity(apiUrlOf(url), request, responseType);
  }

  public void put(String url, Object request) {
    template.put(apiUrlOf(url), request);
  }

  public <T1, T2> ResponseEntity<T2> put(String url, T1 body, Class<T2> responseType) {
    RequestEntity<T1> request = RequestEntity
      .put(URI.create(apiUrlOf(url)))
      .accept(MediaType.APPLICATION_JSON)
      .body(body);
    return template.exchange(request, responseType);
  }

  public <T> ResponseEntity<T> delete(String url, Class<T> responseType) {
    return template.exchange(
      baseUrl + url,
      HttpMethod.DELETE,
      null,
      responseType
    );
  }

  public void delete(String url) {
    template.delete(apiUrlOf(url));
  }

  public <T> Page<T> getPage(String url, Class<T> pagedClass) {
    return getPageResponse(url, pagedClass).getBody().newPage();
  }

  public <T> ResponseEntity<List<T>> getList(String url, Class<T> listedClass) {
    ParameterizedTypeReference<List<T>> responseType = new ParameterizedTypeReference<List<T>>() {
      @Override
      public Type getType() {
        return new ParameterizedTypeReferenceImpl(
          (ParameterizedType) super.getType(),
          new Type[] {listedClass}
        );
      }
    };
    return template.exchange(baseUrl + url, HttpMethod.GET, null, responseType);
  }

  public RestClient loginWith(String username, String password) {
    String authentication = username + ":" + password;
    byte[] authBytes = authentication.getBytes(StandardCharsets.UTF_8);
    String token = new String(Base64.getEncoder().encode(authBytes), StandardCharsets.UTF_8);
    return basicAuthorization(token);
  }

  public RestClient logout() {
    template.getRestTemplate().setInterceptors(
      singletonList((request, body, execution) -> {
        request.getHeaders().remove(AUTHORIZATION);
        return execution.execute(request, body);
      }));
    return this;
  }

  private <T> ResponseEntity<RestResponsePage<T>> getPageResponse(String url, Class<T> pagedClass) {
    ParameterizedTypeReference<RestResponsePage<T>> responseType =
      new ParameterizedTypeReference<RestResponsePage<T>>() {
        @Override
        public Type getType() {
          return new ParameterizedTypeReferenceImpl(
            (ParameterizedType) super.getType(),
            new Type[] {pagedClass}
          );
        }
      };
    return template.exchange(baseUrl + url, HttpMethod.GET, null, responseType);
  }

  private RestClient basicAuthorization(String token) {
    return addHeader(AUTHORIZATION, BASIC + token);
  }

  public RestClient tokenAuthorization() {
    String token = get("/authenticate", UserTokenDto.class).getBody().token;
    return withBearerToken(token);
  }

  public RestClient withBearerToken(String token) {
    return addHeader(AUTHORIZATION, BEARER + token);
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
