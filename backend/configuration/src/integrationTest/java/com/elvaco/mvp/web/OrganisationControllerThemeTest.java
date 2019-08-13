package com.elvaco.mvp.web;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Theme;
import com.elvaco.mvp.core.domainmodels.Theme.ThemeBuilder;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.spi.repository.OrganisationThemes;
import com.elvaco.mvp.database.repository.jpa.OrganisationThemeJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.PropertyDto;
import com.elvaco.mvp.web.dto.UnauthorizedDto;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.web.util.Constants.ACCESS_IS_DENIED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class OrganisationControllerThemeTest extends IntegrationTest {

  private static final String KEY1 = "primary_color";
  private static final String KEY2 = "secondary_color";
  private static final String KEY3 = "other_color";
  private static final String VALUE1 = "#14e5e5";
  private static final String VALUE2 = "#f442e8";
  private static final String VALUE3 = "#FFFFFF";

  @Autowired
  private OrganisationThemes organisationTheme;

  @Autowired
  private OrganisationThemeJpaRepository organisationThemeJpaRepository;

  @Before
  public void before() {
    organisationThemeJpaRepository.deleteAll();
  }

  @Test
  public void getThemeBySlug_notLoggedIn_hasNoTheme() {
    var organisation = given(organisation());
    var response = asNotLoggedIn().getList(slugThemeUrl(organisation), PropertyDto.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void getThemeBySlug_notLoggedIn_hasTheme() {
    var organisation = given(organisation());
    given(theme()
      .organisationId(organisation.id)
      .properties(Map.of(KEY1, VALUE1, KEY2, VALUE2))
    );

    var response = asNotLoggedIn().getList(slugThemeUrl(organisation), PropertyDto.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
      .extracting(tp -> tp.key, tp -> tp.value)
      .containsExactlyInAnyOrder(
        tuple(KEY1, VALUE1),
        tuple(KEY2, VALUE2)
      );
  }

  @Test
  public void getThemeBySlug_inheritFromParentOrganisation() {
    var organisation = given(organisation());
    var subOrganisation = given(subOrganisation().parent(organisation));

    given(theme()
      .organisationId(organisation.id)
      .properties(Map.of(KEY1, VALUE1, KEY2, VALUE2)));
    given(theme()
      .organisationId(subOrganisation.id)
      .properties(Map.of(KEY2, VALUE2 + "new", KEY3, VALUE3)));

    var response = asNotLoggedIn().getList(slugThemeUrl(subOrganisation), PropertyDto.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
      .extracting(tp -> tp.key, tp -> tp.value)
      .containsExactlyInAnyOrder(
        tuple(KEY1, VALUE1),
        tuple(KEY2, VALUE2 + "new"),
        tuple(KEY3, VALUE3)
      );
  }

  @Test
  public void saveTheme() {
    var organisation = given(organisation());
    List<PropertyDto> properties = List.of(
      new PropertyDto(KEY1, VALUE1),
      new PropertyDto(KEY2, VALUE2)
    );

    var response = asSuperAdmin().put(
      idThemeUrl(organisation),
      properties,
      new ParameterizedTypeReference<List<PropertyDto>>() {}
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
      .extracting(p -> p.key, p -> p.value)
      .containsExactlyInAnyOrder(
        tuple(KEY1, VALUE1),
        tuple(KEY2, VALUE2)
      );

    assertThat(organisationTheme.findBy(organisation).properties.entrySet())
      .extracting(Map.Entry::getKey, Map.Entry::getValue)
      .containsExactlyInAnyOrder(
        tuple(KEY1, VALUE1),
        tuple(KEY2, VALUE2)
      );
  }

  @Test
  public void saveTheme_resultIncludesParentProperties() {
    var organisation = given(organisation());
    var subOrganisation = given(subOrganisation().parent(organisation));
    given(theme()
      .organisationId(organisation.id)
      .properties(Map.of(KEY3, VALUE3)));

    List<PropertyDto> properties = List.of(
      new PropertyDto(KEY1, VALUE1),
      new PropertyDto(KEY2, VALUE2)
    );

    var response = asSuperAdmin().put(
      idThemeUrl(subOrganisation),
      properties,
      new ParameterizedTypeReference<List<PropertyDto>>() {}
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
      .extracting(p -> p.key, p -> p.value)
      .containsExactlyInAnyOrder(
        tuple(KEY1, VALUE1),
        tuple(KEY2, VALUE2),
        tuple(KEY3, VALUE3)
      );

    assertThat(organisationTheme.findBy(organisation).properties.entrySet())
      .extracting(Map.Entry::getKey, Map.Entry::getValue)
      .containsExactlyInAnyOrder(
        tuple(KEY3, VALUE3)
      );

    assertThat(organisationTheme.findBy(subOrganisation).properties.entrySet())
      .extracting(Map.Entry::getKey, Map.Entry::getValue)
      .containsExactlyInAnyOrder(
        tuple(KEY1, VALUE1),
        tuple(KEY2, VALUE2),
        tuple(KEY3, VALUE3)
      );
  }

  @Test
  public void saveTheme_updateRemovesAllExisting() {
    var organisation = given(organisation());
    given(theme()
      .organisationId(organisation.id)
      .properties(Map.of(KEY1, VALUE1))
    );

    List<PropertyDto> properties = List.of(new PropertyDto(KEY2, VALUE2));

    var response = asSuperAdmin().put(
      idThemeUrl(organisation),
      properties,
      new ParameterizedTypeReference<List<PropertyDto>>() {}
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
      .extracting(p -> p.key, p -> p.value)
      .containsExactlyInAnyOrder(tuple(KEY2, VALUE2));

    assertThat(organisationTheme.findBy(organisation).properties.entrySet())
      .extracting(Map.Entry::getKey, Map.Entry::getValue)
      .containsExactlyInAnyOrder(tuple(KEY2, VALUE2));
  }

  @Test
  public void saveTheme_notAllowedForAdmin() {
    Organisation organisation = given(organisation());
    User user = given(mvpUser().organisation(organisation).asMvpAdmin());
    List<PropertyDto> properties = List.of(new PropertyDto(KEY1, VALUE1));

    var response = as(user).put(
      idThemeUrl(organisation),
      properties,
      UnauthorizedDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(organisationThemeJpaRepository.findAll()).isEmpty();
  }

  @Test
  public void saveTheme_notAllowedForUser() {
    Organisation organisation = given(organisation());
    User user = given(mvpUser().organisation(organisation).asMvpUser());
    List<PropertyDto> properties = List.of(new PropertyDto(KEY1, VALUE1));

    var response = as(user).put(idThemeUrl(organisation), properties, Unauthorized.class);

    assertAccessIsDenied(response);
    assertThat(organisationThemeJpaRepository.findAll()).isEmpty();
  }

  @Test
  public void deleteTheme() {
    var organisation = given(organisation());
    given(theme()
      .organisationId(organisation.id)
      .properties(Map.of(KEY1, VALUE1))
    );
    assertThat(organisationThemeJpaRepository.findAll()).hasSize(1);

    var response = asSuperAdmin().delete(idThemeUrl(organisation), Unauthorized.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(organisationThemeJpaRepository.findAll()).isEmpty();
  }

  @Test
  public void deleteTheme_notAllowedForAdmin() {
    Organisation organisation = given(organisation());
    User user = given(mvpUser().organisation(organisation).asMvpAdmin());
    given(theme()
      .organisationId(organisation.id)
      .properties(Map.of(KEY1, VALUE1))
    );

    var response = as(user).delete(idThemeUrl(organisation), Unauthorized.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(organisationThemeJpaRepository.findAll()).hasSize(1);
  }

  @Test
  public void deleteTheme_notAllowedForUser() {
    Organisation organisation = given(organisation());
    User user = given(mvpUser().organisation(organisation));
    given(theme()
      .organisationId(organisation.id)
      .properties(Map.of(KEY1, VALUE1))
    );

    var response = as(user).delete(idThemeUrl(organisation), Unauthorized.class);

    assertAccessIsDenied(response);
    assertThat(organisationThemeJpaRepository.findAll()).hasSize(1);
  }

  private Theme given(ThemeBuilder themeBuilder) {
    return organisationTheme.save(themeBuilder.build());
  }

  private ThemeBuilder theme() {
    return Theme.builder()
      .organisationId(context().defaultOrganisation().id);
  }

  private String slugThemeUrl(Organisation organisation) {
    return Url.builder()
      .path("/organisations/" + organisation.slug + "/theme").build().template();
  }

  private String idThemeUrl(Organisation organisation) {
    return Url.builder()
      .path("/organisations/" + organisation.id + "/theme").build().template();
  }

  private static void assertAccessIsDenied(ResponseEntity<Unauthorized> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getMessage()).isEqualTo(ACCESS_IS_DENIED);
  }
}
