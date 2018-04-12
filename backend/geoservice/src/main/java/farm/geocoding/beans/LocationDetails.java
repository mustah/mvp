
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
@JsonPropertyOrder({ "elevation", "timezone_long", "timezone_short" })
public class LocationDetails {

  @JsonProperty("elevation")
  private String elevation;
  @JsonProperty("timezone_long")
  private String timezoneLong;
  @JsonProperty("timezone_short")
  private String timezoneShort;
  @JsonIgnore
  private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("elevation")
  public String getElevation() {
    return elevation;
  }

  @JsonProperty("elevation")
  public void setElevation(String elevation) {
    this.elevation = elevation;
  }

  @JsonProperty("timezone_long")
  public String getTimezoneLong() {
    return timezoneLong;
  }

  @JsonProperty("timezone_long")
  public void setTimezoneLong(String timezoneLong) {
    this.timezoneLong = timezoneLong;
  }

  @JsonProperty("timezone_short")
  public String getTimezoneShort() {
    return timezoneShort;
  }

  @JsonProperty("timezone_short")
  public void setTimezoneShort(String timezoneShort) {
    this.timezoneShort = timezoneShort;
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
