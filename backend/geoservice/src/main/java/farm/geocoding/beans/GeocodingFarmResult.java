
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
@JsonPropertyOrder({ "geocoding_results" })
public class GeocodingFarmResult {

  @JsonProperty("geocoding_results")
  private GeocodingResults geocodingResults;
  @JsonIgnore
  private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("geocoding_results")
  public GeocodingResults getGeocodingResults() {
    return geocodingResults;
  }

  @JsonProperty("geocoding_results")
  public void setGeocodingResults(GeocodingResults geocodingResults) {
    this.geocodingResults = geocodingResults;
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
