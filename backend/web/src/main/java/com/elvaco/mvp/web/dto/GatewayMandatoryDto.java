package com.elvaco.mvp.web.dto;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import static com.elvaco.mvp.core.util.Json.toJsonNode;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatewayMandatoryDto {

  public UUID id;
  public String productModel;
  public String serial;
  public IdNamedDto status;
  public String statusChanged;
  public String ip;
  public String phoneNumber;

  @Default
  public JsonNode extraInfo = toJsonNode("{}");
}
