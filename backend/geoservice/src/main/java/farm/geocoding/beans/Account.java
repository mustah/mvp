
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
@JsonPropertyOrder({ "ip_address", "distribution_license", "usage_limit", "used_today",
    "used_total", "first_used" })
public class Account {

  @JsonProperty("ip_address")
  private String ipAddress;
  @JsonProperty("distribution_license")
  private String distributionLicense;
  @JsonProperty("usage_limit")
  private String usageLimit;
  @JsonProperty("used_today")
  private String usedToday;
  @JsonProperty("used_total")
  private String usedTotal;
  @JsonProperty("first_used")
  private String firstUsed;
  @JsonIgnore
  private final Map<String, Object> additionalProperties = new HashMap<>();

  @JsonProperty("ip_address")
  public String getIpAddress() {
    return ipAddress;
  }

  @JsonProperty("ip_address")
  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  @JsonProperty("distribution_license")
  public String getDistributionLicense() {
    return distributionLicense;
  }

  @JsonProperty("distribution_license")
  public void setDistributionLicense(String distributionLicense) {
    this.distributionLicense = distributionLicense;
  }

  @JsonProperty("usage_limit")
  public String getUsageLimit() {
    return usageLimit;
  }

  @JsonProperty("usage_limit")
  public void setUsageLimit(String usageLimit) {
    this.usageLimit = usageLimit;
  }

  @JsonProperty("used_today")
  public String getUsedToday() {
    return usedToday;
  }

  @JsonProperty("used_today")
  public void setUsedToday(String usedToday) {
    this.usedToday = usedToday;
  }

  @JsonProperty("used_total")
  public String getUsedTotal() {
    return usedTotal;
  }

  @JsonProperty("used_total")
  public void setUsedTotal(String usedTotal) {
    this.usedTotal = usedTotal;
  }

  @JsonProperty("first_used")
  public String getFirstUsed() {
    return firstUsed;
  }

  @JsonProperty("first_used")
  public void setFirstUsed(String firstUsed) {
    this.firstUsed = firstUsed;
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
