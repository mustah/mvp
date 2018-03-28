
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
@JsonPropertyOrder({ "northeast_latitude", "northeast_longitude", "southwest_latitude",
    "southwest_longitude" })
public class Boundaries {

  @JsonProperty("northeast_latitude")
  private String northeastLatitude;
  @JsonProperty("northeast_longitude")
  private String northeastLongitude;
  @JsonProperty("southwest_latitude")
  private String southwestLatitude;
  @JsonProperty("southwest_longitude")
  private String southwestLongitude;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("northeast_latitude")
  public String getNortheastLatitude() {
    return northeastLatitude;
  }

  @JsonProperty("northeast_latitude")
  public void setNortheastLatitude(String northeastLatitude) {
    this.northeastLatitude = northeastLatitude;
  }

  @JsonProperty("northeast_longitude")
  public String getNortheastLongitude() {
    return northeastLongitude;
  }

  @JsonProperty("northeast_longitude")
  public void setNortheastLongitude(String northeastLongitude) {
    this.northeastLongitude = northeastLongitude;
  }

  @JsonProperty("southwest_latitude")
  public String getSouthwestLatitude() {
    return southwestLatitude;
  }

  @JsonProperty("southwest_latitude")
  public void setSouthwestLatitude(String southwestLatitude) {
    this.southwestLatitude = southwestLatitude;
  }

  @JsonProperty("southwest_longitude")
  public String getSouthwestLongitude() {
    return southwestLongitude;
  }

  @JsonProperty("southwest_longitude")
  public void setSouthwestLongitude(String southwestLongitude) {
    this.southwestLongitude = southwestLongitude;
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
