package com.elvaco.rabbitmq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Elvaco MVP MQ message schema version 0.1.
 * <p/>
 * This schema describes the format of the type of message that constitutes the protocol from a
 * metering value delivery system and the MVP system.
 */
public class MeterMessage {

  /**
   * A description of the message type.
   * (Required)
   */
  @SerializedName("message_type")
  @Expose
  private MeterMessage.MessageType messageType;

  /**
   * An identifier for the gateway that delivered the measurements contained in this message.
   * (Required)
   */
  @SerializedName("gateway_id")
  @Expose
  private String gatewayId;
  /**
   * An identifier for the meter, should be unique within an organisation.
   * (Required)
   */
  @SerializedName("meter_id")
  @Expose
  private String meterId;
  /**
   * An identifier for the organisation that owns the meter for which this message was created.
   * (Required)
   */
  @SerializedName("organisation_id")
  @Expose
  private String organisationId;
  /**
   * Describes the device type or medium that the meter for which this message was created handles
   * (e.g temperature).
   * (Required)
   */
  @SerializedName("medium")
  @Expose
  private String medium;
  /**
   * An identifier for the system where this message originated.
   * (Required)
   */
  @SerializedName("source_system_id")
  @Expose
  private String sourceSystemId;
  /**
   * A collection of one or more measurements, likely of different units and/or quantities.
   * (Required)
   */
  @SerializedName("values")
  @Expose
  private List<Value> values = null;

  /**
   * A description of the message type.
   * (Required)
   */
  public MeterMessage.MessageType getMessageType() {
    return messageType;
  }

  /**
   * A description of the message type.
   * (Required)
   */
  public void setMessageType(MeterMessage.MessageType messageType) {
    this.messageType = messageType;
  }

  public MeterMessage withMessageType(MeterMessage.MessageType messageType) {
    this.messageType = messageType;
    return this;
  }

  /**
   * An identifier for the gateway that delivered the measurements contained in this message.
   * (Required)
   */
  public String getGatewayId() {
    return gatewayId;
  }

  /**
   * An identifier for the gateway that delivered the measurements contained in this message.
   * (Required)
   */
  public void setGatewayId(String gatewayId) {
    this.gatewayId = gatewayId;
  }

  public MeterMessage withGatewayId(String gatewayId) {
    this.gatewayId = gatewayId;
    return this;
  }

  /**
   * An identifier for the meter, should be unique within an organisation.
   * (Required)
   */
  public String getMeterId() {
    return meterId;
  }

  /**
   * An identifier for the meter, should be unique within an organisation.
   * (Required)
   */
  public void setMeterId(String meterId) {
    this.meterId = meterId;
  }

  public MeterMessage withMeterId(String meterId) {
    this.meterId = meterId;
    return this;
  }

  /**
   * An identifier for the organisation that owns the meter for which this message was created.
   * (Required)
   */
  public String getOrganisationId() {
    return organisationId;
  }

  /**
   * An identifier for the organisation that owns the meter for which this message was created.
   * (Required)
   */
  public void setOrganisationId(String organisationId) {
    this.organisationId = organisationId;
  }

  public MeterMessage withOrganisationId(String organisationId) {
    this.organisationId = organisationId;
    return this;
  }

  /**
   * Describes the device type or medium that the meter for which this message was created handles
   * (e.g temperature).
   * (Required)
   */
  public String getMedium() {
    return medium;
  }

  /**
   * Describes the device type or medium that the meter for which this message was created handles
   * (e.g temperature).
   * (Required)
   */
  public void setMedium(String medium) {
    this.medium = medium;
  }

  public MeterMessage withMedium(String medium) {
    this.medium = medium;
    return this;
  }

  /**
   * An identifier for the system where this message originated.
   * (Required)
   */
  public String getSourceSystemId() {
    return sourceSystemId;
  }

  /**
   * An identifier for the system where this message originated.
   * (Required)
   */
  public void setSourceSystemId(String sourceSystemId) {
    this.sourceSystemId = sourceSystemId;
  }

  public MeterMessage withSourceSystemId(String sourceSystemId) {
    this.sourceSystemId = sourceSystemId;
    return this;
  }

  /**
   * A collection of one or more measurements, likely of different units and/or quantities.
   * (Required)
   */
  public List<Value> getValues() {
    return values;
  }

  /**
   * A collection of one or more measurements, likely of different units and/or quantities.
   * (Required)
   */
  public void setValues(List<Value> values) {
    this.values = values;
  }

  public MeterMessage withValues(List<Value> values) {
    this.values = values;
    return this;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("messageType", messageType)
      .append(
        "gatewayId",
        gatewayId
      )
      .append("meterId", meterId)
      .append("organisationId", organisationId)
      .append("medium", medium)
      .append("sourceSystemId", sourceSystemId)
      .append("values", values)
      .toString();
  }

  public enum MessageType {

    @SerializedName("Elvaco MVP MQ Message 1.0")
    ELVACO_MVP_MQ_MESSAGE_1_0("Elvaco MVP MQ Message 1.0");
    private final String value;

    private static final Map<String, MeterMessage.MessageType> CONSTANTS = new HashMap<>();

    static {
      for (MeterMessage.MessageType c : values()) {
        CONSTANTS.put(c.value, c);
      }
    }

    MessageType(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return this.value;
    }

    public String value() {
      return this.value;
    }

    public static MeterMessage.MessageType fromValue(String value) {
      MeterMessage.MessageType constant = CONSTANTS.get(value);
      if (constant == null) {
        throw new IllegalArgumentException(value);
      } else {
        return constant;
      }
    }

  }
}