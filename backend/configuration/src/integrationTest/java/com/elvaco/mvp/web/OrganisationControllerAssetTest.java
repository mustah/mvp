package com.elvaco.mvp.web;

import java.io.IOException;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.AssetType;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.ErrorMessageDto;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class OrganisationControllerAssetTest extends IntegrationTest {

  private static Map<AssetType, byte[]> assetFixtures;

  @BeforeClass
  public static void setUp() throws IOException {
    assetFixtures = Map.of(
      AssetType.LOGIN_LOGOTYPE, new ClassPathResource("assets/login_logotype.svg")
        .getInputStream()
        .readAllBytes(),
      AssetType.LOGIN_BACKGROUND, new ClassPathResource("assets/login_background.jpg")
        .getInputStream()
        .readAllBytes(),
      AssetType.LOGOTYPE, new ClassPathResource("assets/logotype.svg")
        .getInputStream()
        .readAllBytes()
    );
  }

  protected abstract AssetType assetUnderTest();

  @Test
  public void user_CannotUpload_OwnOrganisation() {
    var organisation = given(organisation());
    var user = given(user().organisation(organisation));

    assertCannotUpload(user, organisation, HttpStatus.NOT_FOUND);
  }

  @Test
  public void user_CannotUpload_AnyOrganisation() {
    var organisation = given(organisation());
    var user = given(user());

    assertCannotUpload(user, organisation, HttpStatus.NOT_FOUND);
  }

  @Test
  public void user_CanViewAnyAsset() {
    var secretOrganisation = given(organisation());

    var createOneOrganisationsAsset = asSuperAdmin()
      .putFile(
        assetPutUrl(secretOrganisation),
        "asset",
        "logo_to_upload.jpg",
        Object.class
      );

    assertThat(createOneOrganisationsAsset.getStatusCode()).isEqualTo(HttpStatus.OK);

    var anotherOrganisation = given(organisation());
    var userFromOtherOrganisation = given(user().organisation(anotherOrganisation));

    var response = as(userFromOtherOrganisation)
      .get(assetGetUrl(secretOrganisation.slug), byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void loggedOut_CanViewAnyAsset() {
    var secretOrganisation = given(organisation());

    var createOneOrganisationsAsset = asSuperAdmin()
      .putFile(
        assetPutUrl(secretOrganisation),
        "asset",
        "logo_to_upload.jpg",
        Object.class
      );

    assertThat(createOneOrganisationsAsset.getStatusCode()).isEqualTo(HttpStatus.OK);

    var response = asNotLoggedIn()
      .get(assetGetUrl(secretOrganisation.slug), byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void asset_FallbackAssetsAreNotEmpty() {
    var response = asUser()
      .get(assetGetUrl("any-organisation"), byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().length).isGreaterThan(10);
  }

  @Test
  public void fallback_InvalidOrganisationSlug() {
    var response = asUser()
      .get(assetGetUrl("asdf-asdf-invalid-stuff"), byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSameSizeAs(assetFixtures.get(assetUnderTest()));
  }

  @Test
  public void fallback_OrganisationWithoutCustomAsset() {
    var organisationWithoutCustomAsset = given(organisation());
    var userFromOrganisationWithoutCustomAsset = given(
      user().organisation(organisationWithoutCustomAsset)
    );
    var response = as(userFromOrganisationWithoutCustomAsset)
      .get(assetGetUrl(organisationWithoutCustomAsset.slug), byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSameSizeAs(assetFixtures.get(assetUnderTest()));
  }

  @Test
  public void admin_OwnOrganisation_Put() {
    var organisation = given(organisation());
    var admin = given(user().organisation(organisation).asAdmin());

    assertCannotUpload(admin, organisation, HttpStatus.FORBIDDEN);
  }

  @Test
  public void admin_OwnOrganisation_Delete() {
    var organisation = given(organisation());
    var admin = given(user().organisation(organisation).asAdmin());
    assertCannotDelete(admin, organisation, HttpStatus.FORBIDDEN);
  }

  @Test
  public void admin_OwnSubOrganisation_Delete() {
    var parent = given(organisation().name("parent"));
    var admin = given(user().organisation(parent).asAdmin());
    var subOrganisation = given(subOrganisation(parent, admin).name("sub"));
    assertCannotDelete(admin, subOrganisation, HttpStatus.FORBIDDEN);
  }

  @Test
  public void admin_OtherOrganisation_Delete() {
    var organisation = given(organisation());
    var admin = given(user().asAdmin());
    assertCannotDelete(admin, organisation, HttpStatus.NOT_FOUND);
  }

  @Test
  public void admin_OtherSubOrganisation_Delete() {
    var adminsOrganisation = given(organisation());
    var admin = given(user().organisation(adminsOrganisation).asAdmin());

    var otherOrganisationsParent = given(organisation().name("parent"));
    var otherOrganisationsAdmin = given(user().organisation(otherOrganisationsParent).asAdmin());
    var otherSubOrganisation = given(
      subOrganisation(otherOrganisationsParent, otherOrganisationsAdmin).name("sub")
    );
    assertCannotDelete(admin, otherSubOrganisation, HttpStatus.NOT_FOUND);
  }

  @Test
  public void admin_OtherOrganisation_Put() {
    var organisation = given(organisation());
    var admin = given(user().asAdmin());
    assertCannotUpload(admin, organisation, HttpStatus.NOT_FOUND);
  }

  @Test
  public void admin_OwnSubOrganisation_Put() {
    var parent = given(organisation().name("parent"));
    var admin = given(user().organisation(parent).asAdmin());
    var subOrganisation = given(subOrganisation(parent, admin).name("sub"));

    assertCannotUpload(admin, subOrganisation, HttpStatus.FORBIDDEN);
  }

  @Test
  public void superAdmin_NotImage() {
    var organisation = given(organisation());

    var request = asSuperAdmin()
      .putFile(
        assetPutUrl(organisation),
        "asset",
        "not_an_image.txt",
        ErrorMessageDto.class
      );

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(request.getBody().message)
      .contains("You used text/plain but the image needs to be one of ");
  }

  @Test
  public void superAdmin_AnyOrganisation() {
    var organisation = given(organisation());

    var request = asSuperAdmin()
      .putFile(
        assetPutUrl(organisation),
        "asset",
        "logo_to_upload.jpg",
        Object.class
      );

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void superAdmin_RemoveAsset_AnyOrganisation() {
    var organisation = given(organisation());
    var superAdmin = given(user().asSuperAdmin());
    assertCanDelete(superAdmin, organisation);
  }

  @Test
  public void etag_Present() {
    var response = asNotLoggedIn()
      .get(assetGetUrl("some-organisation"), byte[].class);

    assertThat(response.getHeaders()).containsKey("ETag");
  }

  @Test
  public void etag_NoChanges() {
    Url.UrlBuilder url = assetGetUrl("some-organisation");
    var response = asNotLoggedIn()
      .get(url, byte[].class);

    var etag = response.getHeaders().getETag();

    var headers = new HttpHeaders();
    headers.add("If-None-Match", etag);

    var cachedResponse = asNotLoggedIn()
      .get(url, headers, byte[].class);

    assertThat(cachedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED);
    assertThat(cachedResponse.getBody()).isNull();
  }

  @Test
  public void etag_Changed() {
    var organisation = given(organisation().slug("some-organisation"));
    var response = asNotLoggedIn()
      .get(assetGetUrl(organisation.slug), byte[].class);

    var etag = response.getHeaders().getETag();

    var modified = asSuperAdmin()
      .putFile(
        assetPutUrl(organisation),
        "asset",
        "logo_to_upload.jpg",
        Object.class
      );

    assertThat(modified.getStatusCode()).isEqualTo(HttpStatus.OK);

    var headers = new HttpHeaders();
    headers.add("If-None-Match", etag);

    var responseAfterInvalidatedCache = asNotLoggedIn()
      .get(assetGetUrl(organisation.slug), headers, byte[].class);

    assertThat(responseAfterInvalidatedCache.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseAfterInvalidatedCache.getHeaders().getETag()).isNotEqualTo(etag);
    assertThat(responseAfterInvalidatedCache.getBody()).isNotNull();
  }

  private void assertCannotUpload(User user, Organisation organisation, HttpStatus expectedStatus) {
    var request = as(user)
      .putFile(
        assetPutUrl(organisation),
        "asset",
        "logo_to_upload.jpg",
        ErrorMessageDto.class
      );

    assertThat(request.getStatusCode()).isEqualTo(expectedStatus);
  }

  private void assertCanDelete(User user, Organisation organisation) {
    var created = asSuperAdmin()
      .putFile(
        assetPutUrl(organisation),
        "asset",
        "logo_to_upload.jpg",
        Object.class
      );

    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);

    var lookingAtNew = asUser()
      .get(assetGetUrl(organisation.slug), byte[].class);

    assertThat(lookingAtNew.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(lookingAtNew.getBody()).isNotEqualTo(assetFixtures.get(assetUnderTest()));

    var deleted = as(user)
      .delete(assetDeleteUrl(organisation).build(), Object.class);

    assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.OK);

    var lookingAtFallback = asUser()
      .get(assetGetUrl(organisation.slug), byte[].class);

    assertThat(lookingAtFallback.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(lookingAtFallback.getBody()).isEqualTo(assetFixtures.get(assetUnderTest()));
  }

  private void assertCannotDelete(
    User user,
    Organisation organisation,
    HttpStatus expectedStatus
  ) {
    var created = asSuperAdmin()
      .putFile(
        assetPutUrl(organisation),
        "asset",
        "logo_to_upload.jpg",
        Object.class
      );

    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);

    var lookingAtNew = asUser()
      .get(assetGetUrl(organisation.slug), byte[].class);

    var newAsset = lookingAtNew.getBody();

    assertThat(lookingAtNew.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(newAsset).isNotEqualTo(assetFixtures.get(assetUnderTest()));

    var deleted = as(user)
      .delete(assetDeleteUrl(organisation).build(), ErrorMessageDto.class);

    assertThat(deleted.getStatusCode()).isEqualTo(expectedStatus);

    var lookingAtFallback = asUser()
      .get(assetGetUrl(organisation.slug), byte[].class);

    assertThat(lookingAtFallback.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(lookingAtFallback.getBody()).isEqualTo(newAsset);
  }

  private Url.UrlBuilder assetDeleteUrl(Organisation organisation) {
    return assetPutUrl(organisation);
  }

  private Url.UrlBuilder assetPutUrl(Organisation organisation) {
    return Url.builder()
      .path("/organisations/" + organisation.id + "/assets/" + assetUnderTest());
  }

  private Url.UrlBuilder assetGetUrl(String slug) {
    return Url.builder().path("/organisations/" + slug + "/assets/" + assetUnderTest());
  }
}
