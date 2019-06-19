package com.elvaco.mvp.web;

import java.io.IOException;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.AssetType;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.OrganisationDto;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class OrganisationControllerAssetTest extends IntegrationTest {

  private static final String CUSTOM_ASSET_FILENAME = "logo_to_upload.jpg";
  private static final String DIFFERENT_CUSTOM_ASSET_FILENAME = "random_numbers.jpg";
  private static Map<AssetType, byte[]> assetFixtures;
  private static Map<String, byte[]> localFixtures;

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

    localFixtures = Map.of(
      CUSTOM_ASSET_FILENAME, new ClassPathResource("logo_to_upload.jpg")
        .getInputStream()
        .readAllBytes(),
      DIFFERENT_CUSTOM_ASSET_FILENAME, new ClassPathResource("random_numbers.jpg")
        .getInputStream()
        .readAllBytes()
    );
  }

  protected abstract AssetType assetUnderTest();

  @Test
  public void user_CannotUpload_OwnOrganisation() {
    var organisation = given(organisation());
    var user = given(mvpUser().organisation(organisation));

    assertCannotUpload(user, organisation, HttpStatus.NOT_FOUND);
  }

  @Test
  public void user_CannotUpload_AnyOrganisation() {
    var organisation = given(organisation());
    var user = given(mvpUser());

    assertCannotUpload(user, organisation, HttpStatus.NOT_FOUND);
  }

  @Test
  public void user_CanViewAnyAsset() {
    var secretOrganisation = given(organisation());

    putCustomAsset(secretOrganisation);

    var anotherOrganisation = given(organisation());
    var userFromOtherOrganisation = given(mvpUser().organisation(anotherOrganisation));

    var response = as(userFromOtherOrganisation)
      .get(assetGetUrl(secretOrganisation.slug), byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void loggedOut_CanViewAnyAsset() {
    var secretOrganisation = given(organisation());

    putCustomAsset(secretOrganisation);

    var response = asNotLoggedIn()
      .get(assetGetUrl(secretOrganisation.slug), byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void asset_FallbackAssetsAreNotEmpty() {
    var response = asMvpUser()
      .get(assetGetUrl("any-organisation"), byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().length).isGreaterThan(10);
  }

  @Test
  public void fallback_InvalidOrganisationSlug() {
    var response = asMvpUser()
      .get(assetGetUrl("asdf-asdf-invalid-stuff"), byte[].class);

    assertDefaultAsset(response);
  }

  @Test
  public void fallback_OrganisationWithoutCustomAsset() {
    var organisationWithoutCustomAsset = given(organisation());
    var userFromOrganisationWithoutCustomAsset = given(
      mvpUser().organisation(organisationWithoutCustomAsset)
    );
    var response = as(userFromOrganisationWithoutCustomAsset)
      .get(assetGetUrl(organisationWithoutCustomAsset.slug), byte[].class);

    assertDefaultAsset(response);
  }

  @Test
  public void subOrganisation_Fallbacks_UsesOwnIfExisting() {
    var parent = given(organisation().name("parent"));
    var admin = given(mvpUser().organisation(parent).asMvpAdmin());
    var subOrganisation = given(subOrganisation(parent, admin).name("sub"));

    putCustomAsset(subOrganisation);

    var response = asNotLoggedIn()
      .get(assetGetUrl(subOrganisation.slug), byte[].class);

    assertCustomAsset(response);
  }

  @Test
  public void subOrganisation_Fallbacks_UsesOwnIfExistingAndParentExists() {
    var parent = given(organisation().name("parent"));
    var admin = given(mvpUser().organisation(parent).asMvpAdmin());
    var subOrganisation = given(subOrganisation(parent, admin).name("sub"));

    putCustomAsset(parent);
    putDifferentCustomAsset(subOrganisation);

    var response = asNotLoggedIn()
      .get(assetGetUrl(subOrganisation.slug), byte[].class);

    assertDifferentCustomAsset(response);
  }

  @Test
  public void subOrganisation_Fallbacks_ParentOrganisationIfNoOwn() {
    var parent = given(organisation().name("parent"));
    var admin = given(mvpUser().organisation(parent).asMvpAdmin());
    var subOrganisation = given(subOrganisation(parent, admin).name("sub"));

    putCustomAsset(parent);

    var response = asNotLoggedIn()
      .get(assetGetUrl(subOrganisation.slug), byte[].class);

    assertCustomAsset(response);
  }

  @Test
  public void subOrganisation_Fallbacks_DefaultAfterOwnAndParent() {
    var parent = given(organisation().name("parent"));
    var admin = given(mvpUser().organisation(parent).asMvpAdmin());
    var subOrganisation = given(subOrganisation(parent, admin).name("sub"));

    var response = asNotLoggedIn()
      .get(assetGetUrl(subOrganisation.slug), byte[].class);

    assertDefaultAsset(response);
  }

  @Test
  public void admin_OwnOrganisation_Put() {
    var organisation = given(organisation());
    var admin = given(mvpUser().organisation(organisation).asMvpAdmin());

    assertCannotUpload(admin, organisation, HttpStatus.FORBIDDEN);
  }

  @Test
  public void admin_OwnOrganisation_Delete() {
    var organisation = given(organisation());
    var admin = given(mvpUser().organisation(organisation).asMvpAdmin());
    assertCannotDelete(admin, organisation, HttpStatus.FORBIDDEN);
  }

  @Test
  public void admin_OwnSubOrganisation_Delete() {
    var parent = given(organisation().name("parent"));
    var admin = given(mvpUser().organisation(parent).asMvpAdmin());
    var subOrganisation = given(subOrganisation(parent, admin).name("sub"));
    assertCannotDelete(admin, subOrganisation, HttpStatus.FORBIDDEN);
  }

  @Test
  public void admin_OtherOrganisation_Delete() {
    var organisation = given(organisation());
    var admin = given(mvpUser().asMvpAdmin());
    assertCannotDelete(admin, organisation, HttpStatus.NOT_FOUND);
  }

  @Test
  public void admin_OtherSubOrganisation_Delete() {
    var adminsOrganisation = given(organisation());
    var admin = given(mvpUser().organisation(adminsOrganisation).asMvpAdmin());

    var parentOrg = given(organisation().name("parent"));
    var otherOrganisationsAdmin = given(mvpUser().organisation(parentOrg).asMvpAdmin());
    var otherSubOrganisation = given(
      subOrganisation(parentOrg, otherOrganisationsAdmin).name("sub")
    );
    assertCannotDelete(admin, otherSubOrganisation, HttpStatus.NOT_FOUND);
  }

  @Test
  public void admin_OtherOrganisation_Put() {
    var organisation = given(organisation());
    var admin = given(mvpUser().asMvpAdmin());
    assertCannotUpload(admin, organisation, HttpStatus.NOT_FOUND);
  }

  @Test
  public void admin_OwnSubOrganisation_Put() {
    var parent = given(organisation().name("parent"));
    var admin = given(mvpUser().organisation(parent).asMvpAdmin());
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

    putCustomAsset(organisation);
  }

  @Test
  public void superAdmin_RemoveAsset_AnyOrganisation() {
    var organisation = given(organisation());
    var superAdmin = given(mvpUser().asSuperAdmin());
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

    putCustomAsset(organisation);

    var headers = new HttpHeaders();
    headers.add("If-None-Match", etag);

    var responseAfterInvalidatedCache = asNotLoggedIn()
      .get(assetGetUrl(organisation.slug), headers, byte[].class);

    assertThat(responseAfterInvalidatedCache.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseAfterInvalidatedCache.getHeaders().getETag()).isNotEqualTo(etag);
    assertThat(responseAfterInvalidatedCache.getBody()).isNotNull();
  }

  @Test
  public void deleteOrganisation_DeletesAssets() {
    var organisation = given(organisation());
    putCustomAsset(organisation);

    var deleted = asSuperAdmin()
      .delete("/organisations/" + organisation.id, OrganisationDto.class);

    assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.OK);

    var shouldBeDeleted = asSuperAdmin()
      .get("/organisations/" + organisation.id, OrganisationDto.class);
    assertThat(shouldBeDeleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    var lookingAtFallback = asMvpUser()
      .get(assetGetUrl(organisation.slug), byte[].class);

    assertAssetMatches(lookingAtFallback, assetFixtures.get(assetUnderTest()));
  }

  private void assertAssetMatches(ResponseEntity<byte[]> response, byte[] bytes) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(bytes);
  }

  private void assertCustomAsset(ResponseEntity<byte[]> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(localFixtures.get(CUSTOM_ASSET_FILENAME));
  }

  private void assertDifferentCustomAsset(ResponseEntity<byte[]> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(localFixtures.get(DIFFERENT_CUSTOM_ASSET_FILENAME));
  }

  private void assertDefaultAsset(ResponseEntity<byte[]> response) {
    assertAssetMatches(response, assetFixtures.get(assetUnderTest()));
  }

  private void assertCannotUpload(User user, Organisation organisation, HttpStatus expectedStatus) {
    var request = as(user)
      .putFile(
        assetPutUrl(organisation),
        "asset",
        CUSTOM_ASSET_FILENAME,
        ErrorMessageDto.class
      );

    assertThat(request.getStatusCode()).isEqualTo(expectedStatus);
  }

  private void assertCanDelete(User user, Organisation organisation) {
    putCustomAsset(organisation);

    var lookingAtNew = asMvpUser()
      .get(assetGetUrl(organisation.slug), byte[].class);

    assertThat(lookingAtNew.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(lookingAtNew.getBody()).isNotEqualTo(assetFixtures.get(assetUnderTest()));

    var deleted = as(user)
      .delete(assetDeleteUrl(organisation).build(), Object.class);

    assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.OK);

    var lookingAtFallback = asMvpUser()
      .get(assetGetUrl(organisation.slug), byte[].class);

    assertAssetMatches(lookingAtFallback, assetFixtures.get(assetUnderTest()));
  }

  private void assertCannotDelete(
    User user,
    Organisation organisation,
    HttpStatus expectedStatus
  ) {
    putCustomAsset(organisation);

    var lookingAtNew = asMvpUser()
      .get(assetGetUrl(organisation.slug), byte[].class);

    var newAsset = lookingAtNew.getBody();

    assertThat(lookingAtNew.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(newAsset).isNotEqualTo(assetFixtures.get(assetUnderTest()));

    var deleted = as(user)
      .delete(assetDeleteUrl(organisation).build(), ErrorMessageDto.class);

    assertThat(deleted.getStatusCode()).isEqualTo(expectedStatus);

    var lookingAtFallback = asMvpUser()
      .get(assetGetUrl(organisation.slug), byte[].class);

    assertAssetMatches(lookingAtFallback, newAsset);
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

  private void putCustomAsset(Organisation organisation) {
    asSuperAdmin().putFile(
      assetPutUrl(organisation),
      "asset",
      CUSTOM_ASSET_FILENAME,
      Object.class
    );
  }

  private void putDifferentCustomAsset(Organisation organisation) {
    asSuperAdmin().putFile(
      assetPutUrl(organisation),
      "asset",
      DIFFERENT_CUSTOM_ASSET_FILENAME,
      Object.class
    );
  }
}
