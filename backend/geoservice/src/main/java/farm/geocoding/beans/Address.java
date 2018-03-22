
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
@JsonPropertyOrder({ "street_number", "street_name", "admin_1", "postal_code", "country" })
public class Address {

  @JsonProperty("street_number")
  private String streetNumber;
  @JsonProperty("street_name")
  private String streetName;
  @JsonProperty("admin_1")
  private String admin1;
  @JsonProperty("postal_code")
  private String postalCode;
  @JsonProperty("country")
  private String country;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("street_number")
  public String getStreetNumber() {
    return streetNumber;
  }

  @JsonProperty("street_number")
  public void setStreetNumber(String streetNumber) {
    this.streetNumber = streetNumber;
  }

  @JsonProperty("street_name")
  public String getStreetName() {
    return streetName;
  }

  @JsonProperty("street_name")
  public void setStreetName(String streetName) {
    this.streetName = streetName;
  }

  @JsonProperty("admin_1")
  public String getAdmin1() {
    return admin1;
  }

  @JsonProperty("admin_1")
  public void setAdmin1(String admin1) {
    this.admin1 = admin1;
  }

  @JsonProperty("postal_code")
  public String getPostalCode() {
    return postalCode;
  }

  @JsonProperty("postal_code")
  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  @JsonProperty("country")
  public String getCountry() {
    return country;
  }

  @JsonProperty("country")
  public void setCountry(String country) {
    this.country = country;
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
