
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
@JsonPropertyOrder({ "https_ssl", "time_taken" })
public class Statistics {

  @JsonProperty("https_ssl")
  private String httpsSsl;
  @JsonProperty("time_taken")
  private String timeTaken;
  @JsonIgnore
  private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("https_ssl")
  public String getHttpsSsl() {
    return httpsSsl;
  }

  @JsonProperty("https_ssl")
  public void setHttpsSsl(String httpsSsl) {
    this.httpsSsl = httpsSsl;
  }

  @JsonProperty("time_taken")
  public String getTimeTaken() {
    return timeTaken;
  }

  @JsonProperty("time_taken")
  public void setTimeTaken(String timeTaken) {
    this.timeTaken = timeTaken;
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
