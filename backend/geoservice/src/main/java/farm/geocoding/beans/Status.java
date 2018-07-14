
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
@JsonPropertyOrder({ "access", "status", "address_provided", "result_count" })
public class Status {

  @JsonProperty("access")
  private String access;
  @JsonProperty("status")
  private String status;
  @JsonProperty("address_provided")
  private String addressProvided;
  @JsonProperty("result_count")
  private Integer resultCount;
  @JsonIgnore
  private final Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("access")
  public String getAccess() {
    return access;
  }

  @JsonProperty("access")
  public void setAccess(String access) {
    this.access = access;
  }

  @JsonProperty("status")
  public String getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(String status) {
    this.status = status;
  }

  @JsonProperty("address_provided")
  public String getAddressProvided() {
    return addressProvided;
  }

  @JsonProperty("address_provided")
  public void setAddressProvided(String addressProvided) {
    this.addressProvided = addressProvided;
  }

  @JsonProperty("result_count")
  public Integer getResultCount() {
    return resultCount;
  }

  @JsonProperty("result_count")
  public void setResultCount(Integer resultCount) {
    this.resultCount = resultCount;
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
