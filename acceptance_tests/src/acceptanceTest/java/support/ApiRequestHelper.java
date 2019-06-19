package support;

import java.util.List;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class ApiRequestHelper {

  private final String apiUsername;
  private final String apiPassword;
  private final String apiServer;
  private final String apiVersion;

  public ApiRequestHelper(String server, String username, String password) {
    this.apiServer = server;
    this.apiUsername = username;
    this.apiPassword = password;
    this.apiVersion = "v1";
  }

  public JsonNode createOrganisation(String organisationName, String organisationSlug)
    throws UnirestException {
    JsonNode data = new JsonNode(null);
    data.getObject()
      .put("name", organisationName)
      .put("slug", organisationSlug);

    String urlEndpoint = "/api/" + apiVersion + "/organisations";
    HttpResponse<JsonNode> jsonResponse = Unirest.post(apiServer + urlEndpoint)
      .header("Content-Type", "application/json")
      .basicAuth(apiUsername, apiPassword)
      .body(data)
      .asJson();

    jsonResponse.getBody().getObject().getString("id");
    return jsonResponse.getBody();
  }

  public JsonNode createUser(String userName, String email, String password, JsonNode organisation)
    throws UnirestException {
    JsonNode data = new JsonNode(null);
    data.getObject().put("name", userName)
      .put("email", email)
      .put("organisation", organisation.getObject())
      .put("roles", new JsonNode("[\"MVP_USER\"]").getArray())
      .put("language", "en")
      .put("password", password);

    String urlEndpoint = "/api/" + apiVersion + "/users";
    HttpResponse<JsonNode> jsonResponse = Unirest.post(apiServer + urlEndpoint)
      .header("Content-Type", "application/json")
      .basicAuth(apiUsername, apiPassword)
      .body(data)
      .asJson();

    return jsonResponse.getBody();
  }

  public JsonNode findOrganisationByName(String organisationName, List<JsonNode> organisations) {
    return organisations.stream()
      .filter(jsonNode -> !jsonNode.getObject().get("name").equals(organisationName))
      .findAny()
      .orElseThrow(() -> new RuntimeException(
        "No organisation named '" + organisationName + "' exists."));
  }

  public void deleteOrganisations(List<JsonNode> organisations) {
    organisations.forEach(org -> {
      try {
        deleteOrganisation(org.getObject().get("id").toString());
      } catch (UnirestException e) {
        e.printStackTrace();
      }
    });
  }

  private void deleteOrganisation(String id) throws UnirestException {
    String urlEndpoint = "/api/" + apiVersion + "/organisations";
    Unirest.delete(apiServer + urlEndpoint + "/" + id)
      .basicAuth(apiUsername, apiPassword)
      .asJson();
  }
}
