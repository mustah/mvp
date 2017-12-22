package com.elvaco.rabbitmq;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Value {

  /**
   * A timestamp in seconds since 1970-01-01 00:00:00 UTC when this measurement was _collected_.
   * (Required)
   */
  @SerializedName("timestamp")
  @Expose
  private String timestamp;
  /**
   * Whether this measurement represents an accumulated value (true) or not (false). The absence of
   * this value is equivalent to specify it with a value of false.
   */
  @SerializedName("accumulated")
  @Expose
  private boolean accumulated = false;
  /**
   * A discrete measurement value.
   * (Required)
   */
  @SerializedName("value")
  @Expose
  private Object value;

  /**
   * The unit of this measurement (e.g m3, wH).
   * (Required)
   */
  @SerializedName("unit")
  @Expose
  private String unit;
  /**
   * A description, in english, of the type of measurement this value represents (e.g volume)
   * (Required)
   */
  @SerializedName("quantity")
  @Expose
  private String quantity;

  /**
   * A timestamp in seconds since 1970-01-01 00:00:00 UTC when this measurement was _collected_.
   * (Required)
   */
  public String getTimestamp() {
    return timestamp;
  }

  /**
   * A timestamp in seconds since 1970-01-01 00:00:00 UTC when this measurement was _collected_.
   * (Required)
   */
  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public Value withTimestamp(String timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * Whether this measurement represents an accumulated value (true) or not (false).
   * The absence of this value is equivalent to specify it with a value of false.
   */
  public boolean isAccumulated() {
    return accumulated;
  }

  /**
   * Whether this measurement represents an accumulated value (true) or not (false).
   * The absence of this value is equivalent to specify it with a value of false.
   */
  public void setAccumulated(boolean accumulated) {
    this.accumulated = accumulated;
  }

  public Value withAccumulated(boolean accumulated) {
    this.accumulated = accumulated;
    return this;
  }

  /**
   * A discrete measurement value.
   * (Required)
   */
  public Object getValue() {
    return value;
  }

  /**
   * A discrete measurement value.
   * (Required)
   */
  public void setValue(Object value) {
    this.value = value;
  }

  public Value withValue(Object value) {
    this.value = value;
    return this;
  }

  /**
   * The unit of this measurement (e.g m3, wH)
   * (Required)
   */
  public String getUnit() {
    return unit;
  }

  /**
   * The unit of this measurement (e.g m3, wH)
   * (Required)
   */
  public void setUnit(String unit) {
    this.unit = unit;
  }

  public Value withUnit(String unit) {
    this.unit = unit;
    return this;
  }

  /**
   * A description, in english, of the type of measurement this value represents (e.g volume)
   * (Required)
   */
  public String getQuantity() {
    return quantity;
  }

  /**
   * A description, in english, of the type of measurement this value represents (e.g volume)
   * (Required)
   */
  public void setQuantity(String quantity) {
    this.quantity = quantity;
  }

  public Value withQuantity(String quantity) {
    this.quantity = quantity;
    return this;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("timestamp", timestamp).append(
      "accumulated",
      accumulated
    ).append("value", value).append("unit", unit).append("quantity", quantity).toString();
  }
}
