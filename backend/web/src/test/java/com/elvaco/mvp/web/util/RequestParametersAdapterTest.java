package com.elvaco.mvp.web.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;

import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.FACILITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.GATEWAY_SERIAL;
import static com.elvaco.mvp.core.spi.data.RequestParameter.LOGICAL_METER_ID;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ORGANISATION;
import static com.elvaco.mvp.testing.security.MockAuthenticatedUser.mvpUser;
import static com.elvaco.mvp.testing.security.MockAuthenticatedUser.superAdmin;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RequestParametersAdapterTest {

  @Test
  public void isEmptyWhenCreatedWithNullMultiMap() {
    assertThat(RequestParametersAdapter.of(null).isEmpty()).isTrue();
  }

  @Test
  public void throwsExceptionWhenRequestParametersAreNull() {
    assertThatThrownBy(() -> new RequestParametersAdapter().setAll(null))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  public void isEmpty() {
    assertThat(new RequestParametersAdapter().isEmpty()).isTrue();
  }

  @Test
  public void createAdapter() {
    RequestParametersAdapter requestParameters = new RequestParametersAdapter();

    requestParameters
      .add(ORGANISATION, "b")
      .add(LOGICAL_METER_ID, "d");

    assertThat(requestParameters.multiValueMap())
      .isEqualTo(ImmutableMultimap.builder()
        .put(ORGANISATION, "b")
        .put(LOGICAL_METER_ID, "d")
        .build()
        .asMap());
  }

  @Test
  public void createFromMultiValueMap() {
    MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
    multiValueMap.add("id", "b");

    RequestParametersAdapter requestParameters =
      (RequestParametersAdapter) RequestParametersAdapter.of(multiValueMap, LOGICAL_METER_ID);

    assertThat(requestParameters.multiValueMap())
      .isEqualTo(ImmutableMultimap.builder()
        .put(LOGICAL_METER_ID, "b")
        .build()
        .asMap());
  }

  @Test
  public void createWithPathVariables() {
    Map<RequestParameter, String> pathVariable = new HashMap<>();
    pathVariable.put(LOGICAL_METER_ID, "b");
    pathVariable.put(ORGANISATION, "d");

    RequestParametersAdapter adapter = new RequestParametersAdapter();
    adapter.setAll(pathVariable);

    assertThat(adapter.multiValueMap())
      .isEqualTo(ImmutableMultimap.builder()
        .put(LOGICAL_METER_ID, "b")
        .put(ORGANISATION, "d")
        .build()
        .asMap());
  }

  @Test
  public void addSameVariableWithMultipleValues() {
    RequestParametersAdapter adapter = new RequestParametersAdapter();
    adapter
      .add(LOGICAL_METER_ID, "b")
      .add(LOGICAL_METER_ID, "c")
      .add(LOGICAL_METER_ID, "d");

    assertThat(adapter.getValues(LOGICAL_METER_ID)).containsExactly("b", "c", "d");
  }

  @Test
  public void createMultiMapOfPathVariableAndRequestParameters() {
    Map<RequestParameter, String> pathVariables = new HashMap<>();
    pathVariables.put(LOGICAL_METER_ID, "b");
    pathVariables.put(ORGANISATION, "d");

    Map<String, List<String>> requestParams = new HashMap<>();
    requestParams.put("facility", asList("x", "y", "z"));
    requestParams.put("gatewaySerial", asList("testing", "to"));

    RequestParametersAdapter adapter =
      (RequestParametersAdapter) RequestParametersAdapter.of(requestParams)
        .setAll(pathVariables);

    assertThat(adapter.multiValueMap())
      .isEqualTo(ImmutableMultimap.builder()
        .put(LOGICAL_METER_ID, "b")
        .put(ORGANISATION, "d")
        .putAll(FACILITY, "x", "y", "z")
        .putAll(GATEWAY_SERIAL, "testing", "to")
        .build()
        .asMap());
  }

  @Test
  public void pathVarsOverridesRequestParams() {
    Map<RequestParameter, String> pathVariables = new HashMap<>();
    pathVariables.put(LOGICAL_METER_ID, "b");
    pathVariables.put(CITY, "d");

    Map<String, List<String>> requestParams = new HashMap<>();
    requestParams.put("city", asList("x", "y", "z"));

    RequestParametersAdapter adapter =
      (RequestParametersAdapter) RequestParametersAdapter.of(requestParams)
        .setAll(pathVariables);

    assertThat(adapter.multiValueMap())
      .isEqualTo(ImmutableMultimap.builder()
        .put(LOGICAL_METER_ID, "b")
        .put(CITY, "d")
        .build()
        .asMap());
  }

  @Test
  public void replaceAllValuesWith() {
    RequestParameters parameters = new RequestParametersAdapter()
      .add(ORGANISATION, "1")
      .add(ORGANISATION, "2")
      .replace(ORGANISATION, "6");

    assertThat(parameters.getValues(ORGANISATION)).containsExactly("6");
  }

  @Test
  public void replaceEmpty() {
    RequestParameters parameters = new RequestParametersAdapter()
      .replace(ORGANISATION, "6");

    assertThat(parameters.getValues(ORGANISATION)).containsExactly("6");
  }

  @Test
  public void transformParameter() {
    RequestParameters parameters = new RequestParametersAdapter()
      .setAll(ORGANISATION, asList("1", "2"))
      .transform(ORGANISATION, LOGICAL_METER_ID);

    assertThat(parameters.hasParam(ORGANISATION)).isFalse();
    assertThat(parameters.getValues(LOGICAL_METER_ID)).containsExactly("1", "2");
  }

  @Test
  public void hasNoValues() {
    RequestParameters parameters = RequestParametersAdapter.of();

    assertThat(parameters.hasValues(ORGANISATION)).isFalse();
  }

  @Test
  public void hasNoValuesWhenEmpty() {
    RequestParameters parameters = RequestParametersAdapter.of()
      .setAll(ORGANISATION, List.of());

    assertThat(parameters.hasValues(ORGANISATION)).isFalse();
  }

  @Test
  public void hasValues() {
    RequestParameters parameters = RequestParametersAdapter.of()
      .setAll(ORGANISATION, List.of("1"));

    assertThat(parameters.hasValues(ORGANISATION)).isTrue();
  }

  @Test
  public void canSetNullList() {
    RequestParameters parameters = RequestParametersAdapter.of().setAll(ORGANISATION, null);

    assertThat(parameters.getValues(ORGANISATION)).isEqualTo(List.of());
  }

  @Test
  public void getNonExistingValuesShouldReturnEmptyList() {
    assertThat(new RequestParametersAdapter().getValues(LOGICAL_METER_ID)).isEmpty();
  }

  @Test
  public void entrySetShouldBeEmptyWhenThereAreNoParameters() {
    assertThat(new RequestParametersAdapter().entrySet()).isEmpty();
    assertThat(RequestParametersAdapter.of(null).entrySet()).isEmpty();
  }

  @Test
  public void setAllIds() {
    UUID id1 = randomUUID();
    UUID id2 = randomUUID();

    RequestParameters parameters = new RequestParametersAdapter()
      .setAllIds(LOGICAL_METER_ID, asList(id1, id2));

    assertThat(parameters.getValues(LOGICAL_METER_ID)).containsExactly(
      id1.toString(),
      id2.toString()
    );
  }

  @Test
  public void replace_ensureOrganisationIsSameAsUserOrganisationWhenNotSuperAdminUser() {
    AuthenticatedUser currentUser = mvpUser();

    RequestParameters parameters = new RequestParametersAdapter()
      .add(ORGANISATION, randomUUID().toString())
      .ensureOrganisationFilters(currentUser);

    assertThat(parameters.getFirst(ORGANISATION))
      .isEqualTo(currentUser.getOrganisationId().toString());
  }

  @Test
  public void add_ensureOrganisationIsSameAsUserOrganisationWhenNotSuperAdminUser() {
    AuthenticatedUser currentUser = mvpUser();
    RequestParameters parameters = new RequestParametersAdapter()
      .ensureOrganisationFilters(currentUser);

    assertThat(parameters.getFirst(ORGANISATION))
      .isEqualTo(currentUser.getOrganisationId().toString());
  }

  @Test
  public void doNotReplaceOrganisationId_ForSuperAdmin_WhenEnsuringOrganisation() {
    String organisationId = randomUUID().toString();
    AuthenticatedUser currentUser = superAdmin();

    RequestParameters parameters = RequestParametersAdapter.forSuperAdmin(Map.of())
      .add(ORGANISATION, organisationId)
      .ensureOrganisationFilters(currentUser);

    assertThat(parameters.getFirst(ORGANISATION)).isEqualTo(organisationId);
  }

  @Test
  public void doNotTouchParameters_ForSuperAdmin_WhenEnsuringOrganisation() {
    AuthenticatedUser currentUser = superAdmin();

    RequestParameters parameters = RequestParametersAdapter.forSuperAdmin(Map.of())
      .ensureOrganisationFilters(currentUser);

    assertThat(parameters.getFirst(ORGANISATION)).isNull();
  }

  @Test
  public void willOverrideAndEnforceUsersOrganisations_WhenCreatingParametersForSuperAdmin() {
    AuthenticatedUser currentUser = mvpUser();

    RequestParameters parameters = RequestParametersAdapter.forSuperAdmin(Map.of())
      .add(ORGANISATION, randomUUID().toString())
      .ensureOrganisationFilters(currentUser);

    assertThat(parameters.getFirst(ORGANISATION))
      .isEqualTo(currentUser.getOrganisationId().toString());
  }

  @Test
  public void ensureOrganisationId_Even_For_SuperAdmin() {
    AuthenticatedUser currentUser = superAdmin();

    RequestParameters parameters = new RequestParametersAdapter()
      .ensureOrganisationFilters(currentUser);

    assertThat(parameters.getFirst(ORGANISATION))
      .isEqualTo(currentUser.getOrganisationId().toString());
  }

  @Test
  public void usesDefaultLimit_whenNoLimitExists() {
    RequestParameters parameters = new RequestParametersAdapter();

    assertThat(parameters.getLimit()).isEqualTo(RequestParameters.DEFAULT_LIMIT);
  }

  @Test
  public void useDefaultLimit_whenLimitIsNotANumber() {
    RequestParameters parameters = new RequestParametersAdapter();
    parameters.add(RequestParameter.LIMIT, "test");

    assertThat(parameters.getLimit()).isEqualTo(RequestParameters.DEFAULT_LIMIT);
  }

  @Test
  public void useDefaultLimit_whenLimitIsANumber() {
    RequestParameters parameters = new RequestParametersAdapter();
    parameters.add(RequestParameter.LIMIT, "123");

    assertThat(parameters.getLimit()).isEqualTo(123);
  }
}
