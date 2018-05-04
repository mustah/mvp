package com.elvaco.geoservice;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import static com.elvaco.geoservice.UriUtils.asDecodedUri;
import static com.elvaco.geoservice.UriUtils.decode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UriUtilsTest {

  @Test
  public void toUriSafe_callbackDecoded() throws URISyntaxException {
    String callbackId = "ae9ad7a8-07e5-4440-aeb1-662cb028753d";
    URI uri = asDecodedUri(
      "http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fv1%2Fgeocodes%2Fcallback%2F" + callbackId
    );

    String url = "http://localhost:8080/api/v1/geocodes/callback/" + callbackId;
    assertThat(uri).isEqualTo(new URI(url));
  }

  @Test
  public void toUriSafe_errorCallbackDecoded() throws URISyntaxException {
    String callbackId = "ae9ad7a8-07e5-4440-aeb1-662cb028753d";
    URI uri = asDecodedUri(
      "http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fv1%2Fgeocodes%2Ferror%2F" + callbackId
    );

    String url = "http://localhost:8080/api/v1/geocodes/error/" + callbackId;
    assertThat(uri).isEqualTo(new URI(url));
  }

  @Test
  public void decodeUrlParameter() {
    assertThat(decode("V%C3%A4xj%C3%B6")).isEqualTo("Växjö");
    assertThat(decode("växjö")).isEqualTo("växjö");
  }

  @Test
  public void throwsExceptionWhenUrlIsEmpty() {
    assertThatThrownBy(() -> decode(null)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> decode("")).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> decode("   ")).isInstanceOf(NullPointerException.class);
  }
}
