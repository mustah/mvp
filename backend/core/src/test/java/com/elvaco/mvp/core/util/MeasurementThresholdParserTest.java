package com.elvaco.mvp.core.util;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.MeasurementThreshold;
import com.elvaco.mvp.core.domainmodels.MeasurementThreshold.Operator;
import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.unitconverter.UnitConverter;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.MeasurementThreshold.Operator.GREATER_THAN;
import static com.elvaco.mvp.core.domainmodels.MeasurementThreshold.Operator.GREATER_THAN_OR_EQUAL;
import static com.elvaco.mvp.core.domainmodels.MeasurementThreshold.Operator.LESS_THAN;
import static com.elvaco.mvp.core.domainmodels.MeasurementThreshold.Operator.LESS_THAN_OR_EQUAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeasurementThresholdParserTest {

  private static final List<Quantity> RECOGNIZED_QUANTITIES = List.of(
    Quantity.ENERGY, Quantity.FORWARD_TEMPERATURE
  );

  private final QuantityProvider provider = name -> RECOGNIZED_QUANTITIES.stream()
    .filter(quantity -> quantity.name.equals(name))
    .findAny()
    .orElse(null);

  private MeasurementThresholdParser parser;

  @Before
  public void setUp() {
    UnitConverter unitConverter = new MockUnitConverter();
    parser = new MeasurementThresholdParser(provider, unitConverter);
  }

  @Test
  public void parse_emptyThrowsException() {
    Assertions.assertThatThrownBy(() -> parser.parse(""))
      .hasMessage("Malformed expression '' for measurement threshold");
  }

  @Test
  public void parse_quantityIsCorrect() {
    assertThat(quantityFrom("Energy > 10 kWh")).isEqualTo(Quantity.ENERGY);
  }

  @Test
  public void parse_comparisonModeIsCorrect() {
    assertThat(operatorFrom("Energy > 10 kWh")).isEqualTo(GREATER_THAN);

    assertThat(operatorFrom("Energy < 10 kWh")).isEqualTo(LESS_THAN);

    assertThat(operatorFrom("Energy >= 10 kWh")).isEqualTo(GREATER_THAN_OR_EQUAL);

    assertThat(operatorFrom("Energy <= 10 kWh")).isEqualTo(LESS_THAN_OR_EQUAL);
  }

  @Test
  public void parse_valueIsCorrect() {
    assertThat(parsedValueFrom("Energy > 10 kWh")).isEqualTo(10.0);

    assertThat(parsedValueFrom("Energy > 10.0 kWh")).isEqualTo(10.0);

    assertThat(parsedValueFrom("Energy > 0 kWh")).isEqualTo(0);

    assertThat(parsedValueFrom("Energy > 0.0 kWh")).isEqualTo(0);

    assertThat(parsedValueFrom("Energy > 1.5 kWh")).isEqualTo(1.5);

    assertThat(parsedValueFrom("Energy > 1e2 kWh")).isEqualTo(100.0);

    assertThat(parsedValueFrom("Energy > 1E2 kWh")).isEqualTo(100.0);

    assertThat(parsedValueFrom("Energy > 1.0e2 kWh")).isEqualTo(100.0);

    assertThat(parsedValueFrom("Energy > 1000000 kWh")).isEqualTo(1000000.0);
  }

  @Test
  public void parse_unitIsCorrect() {
    assertThat(parsedUnitFrom("Energy > 10 kWh")).isEqualTo("kWh");

    assertThat(parsedUnitFrom("Energy > 10 Wh")).isEqualTo("Wh");

    assertThat(parsedUnitFrom("Energy > 10 J")).isEqualTo("J");
  }

  @Test
  public void parse_withDurationExpression() {
    assertThat(parsedDurationFrom("Energy > 10 kWh for 2 days")).isEqualTo(Duration.ofDays(2));
    assertThat(parsedDurationFrom("Energy > 10 kWh for 7 days")).isEqualTo(Duration.ofDays(7));
  }

  @Test
  public void parse_noDuration() {
    assertThat(parsedDurationFrom("Energy > 10 kWh")).isNull();
  }

  @Test
  public void parse_invalidDuration() {
    assertThatThrownBy(() -> parser.parse("Energy > 10 kWh for 0 days"))
      .hasMessageContaining("Invalid duration '0'");

    assertThatThrownBy(() -> parser.parse("Energy > 10 kWh for -99 days"))
      .hasMessageContaining("Malformed expression");

    assertThatThrownBy(() -> parser.parse("Energy > 10 kWh for xyzzy days"))
      .hasMessageContaining("Malformed expression");
  }

  @Test
  public void parse_invalidDimension() {
    UnitConverter unitConverter = new MockUnitConverter(() -> false, (v) -> v);
    parser = new MeasurementThresholdParser(provider, unitConverter);
    assertThatThrownBy(() -> parser.parse("Energy > 10 kWh"))
      .hasMessageContaining("Invalid unit 'kWh' for quantity 'Energy' in measurement threshold");
  }

  @Test
  public void parse_invalidComparisonMode() {

    assertThatThrownBy(() -> parser.parse("Energy >== 10 kWh")).hasMessage(
      "Malformed expression 'Energy >== 10 kWh' for measurement threshold");
    assertThatThrownBy(() -> parser.parse("Energy >< 10 kWh")).hasMessage(
      "Malformed expression 'Energy >< 10 kWh' for measurement threshold");
    assertThatThrownBy(() -> parser.parse("Energy <> 10 kWh")).hasMessage(
      "Malformed expression 'Energy <> 10 kWh' for measurement threshold");
    assertThatThrownBy(() -> parser.parse("Energy = 10 kWh")).hasMessage(
      "Malformed expression 'Energy = 10 kWh' for measurement threshold");
  }

  @Test
  public void parse_withoutWhitespace() {
    assertThat(parser.parse("Energy>10kWh")).isEqualTo(
      new MeasurementThreshold(
        Quantity.ENERGY,
        new MeasurementUnit("kWh", 10.0),
        new MeasurementUnit("kWh", 10.0),
        GREATER_THAN
      )
    );
  }

  @Test
  public void parse_withWhitespace() {
    assertThat(parser.parse("Energy > 10 kWh")).isEqualTo(
      new MeasurementThreshold(
        Quantity.ENERGY,
        new MeasurementUnit("kWh", 10.0),
        new MeasurementUnit("kWh", 10.0),
        GREATER_THAN
      ));
  }

  @Test
  public void parse_withSurroundingWhitespace() {
    assertThat(parser.parse(" \t   Energy > 10 kWh   \t  ")).isEqualTo(
      new MeasurementThreshold(
        Quantity.ENERGY,
        new MeasurementUnit("kWh", 10.0),
        new MeasurementUnit("kWh", 10.0),
        GREATER_THAN
      ));
  }

  @Test
  public void parse_withSpacesInQuantity() {
    assertThat(parser.parse("Forward temperature <= 0 °C")).isEqualTo(
      new MeasurementThreshold(
        Quantity.FORWARD_TEMPERATURE,
        new MeasurementUnit("°C", 0.0),
        new MeasurementUnit("°C", 0.0),
        Operator.LESS_THAN_OR_EQUAL
      ));
  }

  @Test
  public void parse_invalidValue() {
    assertThatThrownBy(() -> parser.parse("Energy <= apa kW")).hasMessage(
      "Malformed expression 'Energy <= apa kW' for measurement threshold");
    assertThatThrownBy(() -> parser.parse("Energy <= kW")).hasMessage(
      "Malformed expression 'Energy <= kW' for measurement threshold");
  }

  @Test
  public void parse_missingTerms() {
    assertThatThrownBy(() -> parser.parse("Forward temperature 0 °C")).hasMessage(
      "Malformed expression 'Forward temperature 0 °C' for measurement threshold");

    assertThatThrownBy(() -> parser.parse("Energy = 10")).hasMessage(
      "Malformed expression 'Energy = 10' for measurement threshold");

    assertThatThrownBy(() -> parser.parse("<= 0 °C")).hasMessage(
      "Malformed expression '<= 0 °C' for measurement threshold");
  }

  @Test
  public void parse_valueUnitConversionIsDone() {
    UnitConverter unitConverter = new MockUnitConverter(
      () -> true,
      (Double v) -> v / 1000.0
    );

    parser = new MeasurementThresholdParser(provider, unitConverter);
    assertThat(parser.parse("Energy > 1000 Wh").convertedValueUnit)
      .isEqualTo(new MeasurementUnit("kWh", 1.0));
  }

  private Duration parsedDurationFrom(String expr) {
    return parser.parse(expr).duration;
  }

  private String parsedUnitFrom(String expr) {
    return Objects.requireNonNull(parser.parse(expr)).getParsedUnit();
  }

  private Quantity quantityFrom(String expr) {
    return Objects.requireNonNull(parser.parse(expr)).quantity;
  }

  private Operator operatorFrom(String expr) {
    return parser.parse(expr).operator;
  }

  private double parsedValueFrom(String expr) {
    return Objects.requireNonNull(parser.parse(expr)).getParsedValue();
  }

  private static class MockUnitConverter implements UnitConverter {

    private final BooleanSupplier dimensionDecisionSupplier;
    private final Function<Double, Double> valueConverter;

    private MockUnitConverter() {
      this(() -> true, (v) -> v);
    }

    private MockUnitConverter(
      BooleanSupplier dimensionDecisionSupplier,
      Function<Double, Double> valueConverter
    ) {
      this.dimensionDecisionSupplier = dimensionDecisionSupplier;
      this.valueConverter = valueConverter;
    }

    @Override
    public MeasurementUnit convert(MeasurementUnit measurementUnit, String targetUnit) {
      return new MeasurementUnit(targetUnit, valueConverter.apply(measurementUnit.getValue()));
    }

    @Override
    public boolean isSameDimension(String firstUnit, String secondUnit) {
      return dimensionDecisionSupplier.getAsBoolean();
    }
  }
}
