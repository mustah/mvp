
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
@JsonPropertyOrder({ "result_number", "formatted_address", "accuracy", "ADDRESS",
    "LOCATION_DETAILS", "COORDINATES", "BOUNDARIES" })
public class Result {

  @JsonProperty("result_number")
  private Integer resultNumber;
  @JsonProperty("formatted_address")
  private String formattedAddress;
  @JsonProperty("accuracy")
  private String accuracy;
  @JsonProperty("ADDRESS")
  private Address address;
  @JsonProperty("LOCATION_DETAILS")
  private LocationDetails locationDetails;
  @JsonProperty("COORDINATES")
  private Coordinates coordinates;
  @JsonProperty("BOUNDARIES")
  private Boundaries boundaries;
  @JsonIgnore
  private final Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("result_number")
  public Integer getResultNumber() {
    return resultNumber;
  }

  @JsonProperty("result_number")
  public void setResultNumber(Integer resultNumber) {
    this.resultNumber = resultNumber;
  }

  @JsonProperty("formatted_address")
  public String getFormattedAddress() {
    return formattedAddress;
  }

  @JsonProperty("formatted_address")
  public void setFormattedAddress(String formattedAddress) {
    this.formattedAddress = formattedAddress;
  }

  @JsonProperty("accuracy")
  public String getAccuracy() {
    return accuracy;
  }

  @JsonProperty("accuracy")
  public void setAccuracy(String accuracy) {
    this.accuracy = accuracy;
  }

  @JsonProperty("ADDRESS")
  public Address getAddress() {
    return address;
  }

  @JsonProperty("ADDRESS")
  public void setAddress(Address address) {
    this.address = address;
  }

  @JsonProperty("LOCATION_DETAILS")
  public LocationDetails getLocationDetails() {
    return locationDetails;
  }

  @JsonProperty("LOCATION_DETAILS")
  public void setLocationDetails(LocationDetails locationDetails) {
    this.locationDetails = locationDetails;
  }

  @JsonProperty("COORDINATES")
  public Coordinates getCoordinates() {
    return coordinates;
  }

  @JsonProperty("COORDINATES")
  public void setCoordinates(Coordinates coordinates) {
    this.coordinates = coordinates;
  }

  @JsonProperty("BOUNDARIES")
  public Boundaries getBoundaries() {
    return boundaries;
  }

  @JsonProperty("BOUNDARIES")
  public void setBoundaries(Boundaries boundaries) {
    this.boundaries = boundaries;
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
