
package farm.geocoding.beans;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "copyright_notice", "copyright_logo", "terms_of_service", "privacy_policy" })
public class LegalCopyright {

  @JsonProperty("copyright_notice")
  private String copyrightNotice;
  @JsonProperty("copyright_logo")
  private String copyrightLogo;
  @JsonProperty("terms_of_service")
  private String termsOfService;
  @JsonProperty("privacy_policy")
  private String privacyPolicy;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("copyright_notice")
  public String getCopyrightNotice() {
    return copyrightNotice;
  }

  @JsonProperty("copyright_notice")
  public void setCopyrightNotice(String copyrightNotice) {
    this.copyrightNotice = copyrightNotice;
  }

  @JsonProperty("copyright_logo")
  public String getCopyrightLogo() {
    return copyrightLogo;
  }

  @JsonProperty("copyright_logo")
  public void setCopyrightLogo(String copyrightLogo) {
    this.copyrightLogo = copyrightLogo;
  }

  @JsonProperty("terms_of_service")
  public String getTermsOfService() {
    return termsOfService;
  }

  @JsonProperty("terms_of_service")
  public void setTermsOfService(String termsOfService) {
    this.termsOfService = termsOfService;
  }

  @JsonProperty("privacy_policy")
  public String getPrivacyPolicy() {
    return privacyPolicy;
  }

  @JsonProperty("privacy_policy")
  public void setPrivacyPolicy(String privacyPolicy) {
    this.privacyPolicy = privacyPolicy;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

}
