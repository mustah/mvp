package support;

import java.util.List;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class ApiRequestHelper {
  private String apiUsername;
  private String apiPassword;
  private String apiServer;
  private String apiVersion = "v1";

  public ApiRequestHelper(String server, String username, String password) {
    apiServer = server;
    apiUsername = username;
    apiPassword = password;
  }

  public JsonNode createOrganisation(String organisationName, String organisationSlug)
    throws UnirestException {
    String urlEndpoint = "/api/" + apiVersion + "/organisations";
    JsonNode data = new JsonNode(null);
    data.getObject()
      .put("name", organisationName)
      .put("slug", organisationSlug);

    HttpResponse<JsonNode> jsonResponse = Unirest.post(apiServer + urlEndpoint)
      .header("Content-Type", "application/json")
      .basicAuth(apiUsername, apiPassword)
      .body(data)
      .asJson();

    jsonResponse.getBody().getObject().getString("id");
    return jsonResponse.getBody();
  }

  public JsonNode createUser(String username, String email, String password, JsonNode organisation)
    throws UnirestException {
    String urlEndpoint = "/api/" + apiVersion + "/users";
    JsonNode data = new JsonNode(null);
    data.getObject().put("name", username)
      .put("email", email)
      .put("organisation", organisation.getObject())
      .put("roles", new JsonNode("[\"USER\"]").getArray())
      .put("language", "en")
      .put("password", password);

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
    organisations.stream().forEach(org -> {
      try {
        deleteOrganisation(org.getObject().get("id").toString());
      } catch (UnirestException e) {
        e.printStackTrace();
      }
    });
  }

  public void deleteOrganisation(String id) throws UnirestException {
    String urlEndpoint = "/api/" + apiVersion + "/organisations";
    Unirest.delete(apiServer + urlEndpoint + "/" + id)
      .basicAuth(apiUsername, apiPassword)
      .asJson();
  }
}
