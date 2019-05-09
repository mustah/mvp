package com.elvaco.mvp.web;

import java.io.IOException;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.AssetType;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.UnauthorizedDto;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
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
  public void user_CannotUpload() {
    var organisation = given(organisation());
    var user = given(user());
    var putResponse = as(user)
      .putFile(
        assetPutUrl(organisation),
        "asset",
        "logo_to_upload.jpg",
        UnauthorizedDto.class
      );

    assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(putResponse.getBody().message).contains("Unable to find organisation with ID");
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
  public void admin_OwnOrganisation_NotImage() {
    var organisation = given(organisation());
    var admin = given(user().organisation(organisation).asAdmin());

    var request = as(admin)
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
  public void admin_OwnOrganisation() {
    var organisation = given(organisation());
    var admin = given(user().organisation(organisation).asAdmin());

    var request = as(admin)
      .putFile(
        assetPutUrl(organisation),
        "asset",
        "logo_to_upload.jpg",
        Object.class
      );

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void admin_SubOrganisation() {
    var parent = given(organisation().name("parent"));
    var admin = given(user().organisation(parent).asAdmin());
    var subOrganisation = given(subOrganisation(parent, admin).name("sub"));

    var request = as(admin)
      .putFile(
        assetPutUrl(subOrganisation),
        "asset",
        "logo_to_upload.jpg",
        Object.class
      );

    assertThat(request.getStatusCode()).isEqualTo(HttpStatus.OK);
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
  public void superAdmin_RemoveAsset() {
    var organisation = given(organisation());

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

    var deleted = asSuperAdmin()
      .delete(assetDeleteUrl(organisation).build(), Object.class);

    assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.OK);

    var lookingAtFallback = asUser()
      .get(assetGetUrl(organisation.slug), byte[].class);

    assertThat(lookingAtFallback.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(lookingAtFallback.getBody()).isEqualTo(assetFixtures.get(assetUnderTest()));
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
