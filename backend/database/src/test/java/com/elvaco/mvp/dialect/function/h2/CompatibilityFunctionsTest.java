package com.elvaco.mvp.dialect.function.h2;

import org.junit.Test;

import static com.elvaco.mvp.dialect.function.h2.CompatibilityFunctions.jsonbContains;
import static com.elvaco.mvp.dialect.function.h2.CompatibilityFunctions.jsonbExists;
import static com.elvaco.mvp.dialect.function.h2.CompatibilityFunctions.toMeasurementUnit;
import static com.elvaco.mvp.dialect.function.h2.CompatibilityFunctions.unitAt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompatibilityFunctionsTest {
  @Test
  public void convertDegreeScale() {
    assertEquals("287.15 K", unitAt("14 Celsius", "K"));
  }

  // Verify that the aliases that we've defined for PostgreSql also work here
  @Test
  public void unitAliasForCelsius() {
    assertEquals("14 ℃", unitAt("14 Celsius", "℃"));
  }

  @Test
  public void unitAliasForKelvin() {
    assertEquals("287.15 K", unitAt("287.15 Kelvin", "K"));
  }

  @Test
  public void unitAliasForCubicMeters() {
    assertEquals("43 ㎥", unitAt("43 m^3", "㎥"));
  }

  @Test
  public void unitAliasForCubicMetersNoCaret() {
    assertEquals("43 ㎥", unitAt("43 m3", "㎥"));
  }

  /* Most test cases taken from
  https://www.postgresql.org/docs/9.5/static/datatype-json.html#JSON-CONTAINMENT */
  @Test
  public void jsonbContainsScalar() {
    assertTrue(jsonbContains("\"foo\"", "\"foo\""));
    assertFalse(jsonbContains("\"foo\"", "\"bar\""));
  }

  @Test
  public void jsonbContainsArray() {
    assertTrue(jsonbContains("[]", "[]"));
    assertTrue(jsonbContains("[[]]", "[]"));
    assertTrue(jsonbContains("[1,2,3]", "[1,2,3]"));
    assertTrue(jsonbContains("[1,2,3]", "[1,3]"));
    assertTrue(jsonbContains("[1,2,3]", "[3,1]"));
    assertFalse(jsonbContains("[1,2,3]", "[1,3,77]"));
  }

  @Test
  public void jsonContains_SpecialExceptions() {
    assertThat(jsonbContains("[\"foo\", \"bar\"]", "\"bar\"")).isTrue();
    assertThat(jsonbContains("\"bar\"", "[\"foo\", \"bar\"]")).isFalse();
  }

  @Test
  public void jsonbContainsArray_OrderIsIgnored() {
    assertThat(jsonbContains("[1,2,3]", "[3,1,2]")).isTrue();
  }

  @Test
  public void jsonbContainsCanHaveDuplicateElements() {
    assertThat(jsonbContains("[1,2,3]", "[3,1,1,1,2]")).isTrue();
  }

  @Test
  public void jsonbContainsArrayNested() {
    assertFalse(jsonbContains("[1,2,[1,3]]", "[1,3]"));
    assertTrue(jsonbContains("[1,2,[1,3]]", "[[1,3]]"));
    assertTrue(jsonbContains("[1,3,[1,3]]", "[1,3]"));
    assertFalse(jsonbContains("[1,2,[3,4,[5]]]", "[[5]]"));
    assertTrue(jsonbContains("[1,2,[3,4,[5]]]", "[[3,4]]"));
    assertTrue(jsonbContains("[1,2,[3,4,[5]]]", "[[[5]]]"));
    assertFalse(jsonbContains("[1,2,[3,4,[[5]]]]", "[[[5]]]"));
    assertTrue(jsonbContains("[1,2,[3,4,[[5]]]]", "[[[[5]]]]"));
  }

  @Test
  public void jsonbContainsObject() {
    assertFalse(jsonbContains("{\"foo\": 1}", "1"));
    assertFalse(jsonbContains("{\"foo\": [1,2,3]}", "[1,2,3]"));
    assertTrue(jsonbContains("{\"foo\": 1}", "{\"foo\": 1}"));
    assertTrue(jsonbContains("{\"foo\": [1,2,3]}", "{\"foo\": [1]}"));
    assertTrue(jsonbContains(
      "{\"product\": \"PostgreSQL\", \"version\": 9.4, \"jsonb\": true}",
      "{\"version\": 9.4}"
    ));
    assertFalse(jsonbContains(
      "{\"product\": \"PostgreSQL\", \"version\": 9.4, \"jsonb\": true}",
      "{\"version\": 9.3}"
    ));
  }

  @Test
  public void jsonbContainsObjectNestedLeft() {
    assertFalse(jsonbContains("{\"foo\": {\"bar\": \"baz\"}}", "{\"bar\": \"baz\"}"));
  }

  @Test
  public void jsonbContainsObjectNestedRight() {
    assertTrue(jsonbContains("{\"foo\": {\"bar\": \"baz\"}}", "{\"foo\": {}}"));
  }

  @Test
  public void jsonbDoesExist() {
    assertTrue(jsonbExists("{\"foo\": {\"bar\": \"baz\"}}", "foo"));
    assertTrue(jsonbExists("[\"foo\"]", "foo"));
    assertTrue(jsonbExists("\"foo\"", "foo"));
  }

  @Test
  public void jsonbDoesNotExist() {
    assertFalse(jsonbExists("{\"foo\": {\"bar\": \"baz\"}}", "xyzzy"));
    assertFalse(jsonbExists("{\"foo\": {\"bar\": \"baz\"}}", "bar"));
    assertFalse(jsonbExists("[[\"bar\"]]", "bar"));
  }

  @Test
  public void measurementUnits() {
    assertThat(toMeasurementUnit("150 °C", "K").toString()).isEqualTo("423.15 K");
  }
}
