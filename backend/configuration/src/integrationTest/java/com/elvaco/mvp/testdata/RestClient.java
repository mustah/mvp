package com.elvaco.mvp.testdata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import com.elvaco.mvp.web.dto.UserTokenDto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.util.DefaultUriBuilderFactory;

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
    DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
    uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
    this.template = new TestRestTemplate(
      new RestTemplateBuilder().uriTemplateHandler(uriBuilderFactory)
    );
  }

  public static String apiPathOf(String url) {
    return API_V1 + url;
  }

  public <T> ResponseEntity<T> get(Url.UrlBuilder builder, Class<T> clazz) {
    var urlTemplate = builder.build();
    return get(urlTemplate.template(), clazz, urlTemplate.variables());
  }

  public <T> ResponseEntity<T> get(UrlTemplate urlTemplate, Class<T> clazz) {
    return get(urlTemplate.template(), clazz, urlTemplate.variables());
  }

  public <T> ResponseEntity<T> get(String url, Class<T> clazz, Object... urlVariables) {
    return template.getForEntity(apiUrlOf(url), clazz, urlVariables);
  }

  public <T> ResponseEntity<T> putFile(
    Url.UrlBuilder url,
    String elementName,
    String localFilename,
    Class<T> responseType
  ) {
    LinkedMultiValueMap<String, Object> parameters = new LinkedMultiValueMap<String, Object>();
    parameters.add(elementName, new ClassPathResource(localFilename));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    HttpEntity<LinkedMultiValueMap<String, Object>> entity =
      new HttpEntity<>(
        parameters,
        headers
      );

    return template.exchange(
      apiUrlOf(url.build().template()),
      HttpMethod.PUT,
      entity,
      responseType,
      ""
    );
  }

  public <T> ResponseEntity<T> post(UrlTemplate url, Object request, Class<T> responseType) {
    return post(url.template(), request, responseType, url.variables());
  }

  public <T> ResponseEntity<T> post(
    String url,
    Object request,
    Class<T> responseType,
    Object... urlVariables
  ) {
    return template.postForEntity(apiUrlOf(url), request, responseType, urlVariables);
  }

  public <T> ResponseEntity<List<T>> postList(
    String url,
    Object requestBody,
    Class<T> listedClass
  ) {
    ParameterizedTypeReference<List<T>> responseType = new ParameterizedTypeReference<>() {
      @Override
      public Type getType() {
        return new ParameterizedTypeReferenceImpl(
          (ParameterizedType) super.getType(),
          new Type[] {listedClass}
        );
      }
    };

    HttpEntity<?> requestEntity;
    if (requestBody instanceof HttpEntity) {
      requestEntity = (HttpEntity<?>) requestBody;
    } else if (requestBody != null) {
      requestEntity = new HttpEntity<>(requestBody);
    } else {
      requestEntity = HttpEntity.EMPTY;
    }

    return template.exchange(baseUrl + url, HttpMethod.POST, requestEntity, responseType);
  }

  public void put(String url, Object request) {
    template.put(apiUrlOf(url), request);
  }

  public <T1, T2> ResponseEntity<T2> put(UrlTemplate urlTemplate, T1 body, Class<T2> responseType) {
    return put(urlTemplate.template(), body, responseType);
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

  public <T> ResponseEntity<T> delete(UrlTemplate urlTemplate, Class<T> responseType) {
    return template.exchange(
      baseUrl + urlTemplate.template(),
      HttpMethod.DELETE,
      null,
      responseType,
      urlTemplate.variables()
    );
  }

  public void delete(String url) {
    template.delete(apiUrlOf(url));
  }

  public <T> Page<T> getPage(UrlTemplate urlTemplate, Class<T> pagedClass) {
    RestResponsePage<T> body = getPageResponse(
      urlTemplate.template(),
      pagedClass,
      urlTemplate.variables()
    ).getBody();
    return body != null ? body.newPage() : new RestResponsePage<>();
  }

  public <T> Page<T> getPage(String url, Class<T> pagedClass, Object... variables) {
    RestResponsePage<T> body = getPageResponse(url, pagedClass, variables).getBody();
    return body != null ? body.newPage() : new RestResponsePage<>();
  }

  public <T> ResponseEntity<List<T>> getList(
    UrlTemplate urlTemplate,
    Class<T> listedClass
  ) {
    return getList(urlTemplate.template(), listedClass, urlTemplate.variables());
  }

  public <T> ResponseEntity<List<T>> getList(
    Url.UrlBuilder urlBuilder,
    Class<T> listedClass
  ) {
    var urlTemplate = urlBuilder.build();
    return getList(urlTemplate.template(), listedClass, urlTemplate.variables());
  }

  public <T> ResponseEntity<List<T>> getList(
    String url,
    Class<T> listedClass,
    Object... urlVariables
  ) {
    ParameterizedTypeReference<List<T>> responseType = new ParameterizedTypeReference<>() {
      @Override
      public Type getType() {
        return new ParameterizedTypeReferenceImpl(
          (ParameterizedType) super.getType(),
          new Type[] {listedClass}
        );
      }
    };
    return template.exchange(baseUrl + url, HttpMethod.GET, null, responseType, urlVariables);
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

  public JsonNode getJson(String url) {
    return template.getForObject(baseUrl + url, ObjectNode.class);
  }

  public RestClient tokenAuthorization() {
    UserTokenDto body = get("/authenticate", UserTokenDto.class).getBody();
    return withBearerToken(body == null ? "" : body.token);
  }

  public RestClient withBearerToken(String token) {
    return addHeader(AUTHORIZATION, BEARER + token);
  }

  private <T> ResponseEntity<RestResponsePage<T>> getPageResponse(
    String url,
    Class<T> pagedClass,
    Object... variables
  ) {
    ParameterizedTypeReference<RestResponsePage<T>> responseType =
      new ParameterizedTypeReference<>() {
        @Override
        public Type getType() {
          return new ParameterizedTypeReferenceImpl(
            (ParameterizedType) super.getType(),
            new Type[] {pagedClass}
          );
        }
      };
    return template.exchange(baseUrl + url, HttpMethod.GET, null, responseType, variables);
  }

  private RestClient basicAuthorization(String token) {
    return addHeader(AUTHORIZATION, BASIC + token);
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
