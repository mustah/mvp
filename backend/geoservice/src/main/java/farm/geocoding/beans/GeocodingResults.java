
package farm.geocoding.beans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "LEGAL_COPYRIGHT", "STATUS", "ACCOUNT", "RESULTS", "STATISTICS" })
public class GeocodingResults {

  @JsonProperty("LEGAL_COPYRIGHT")
  private LegalCopyright legalCopyright;
  @JsonProperty("STATUS")
  private Status status;
  @JsonProperty("ACCOUNT")
  private Account account;
  @JsonProperty("RESULTS")
  private List<Result> results = null;
  @JsonProperty("STATISTICS")
  private Statistics statistics;
  @JsonIgnore
  private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("LEGAL_COPYRIGHT")
  public LegalCopyright getLegalCopyright() {
    return legalCopyright;
  }

  @JsonProperty("LEGAL_COPYRIGHT")
  public void setLegalCopyright(LegalCopyright legalCopyright) {
    this.legalCopyright = legalCopyright;
  }

  @JsonProperty("STATUS")
  public Status getStatus() {
    return status;
  }

  @JsonProperty("STATUS")
  public void setStatus(Status status) {
    this.status = status;
  }

  @JsonProperty("ACCOUNT")
  public Account getAccount() {
    return account;
  }

  @JsonProperty("ACCOUNT")
  public void setAccount(Account account) {
    this.account = account;
  }

  @JsonProperty("RESULTS")
  public List<Result> getResults() {
    return results;
  }

  @JsonProperty("RESULTS")
  public void setResults(List<Result> results) {
    this.results = results;
  }

  @JsonProperty("STATISTICS")
  public Statistics getStatistics() {
    return statistics;
  }

  @JsonProperty("STATISTICS")
  public void setStatistics(Statistics statistics) {
    this.statistics = statistics;
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
