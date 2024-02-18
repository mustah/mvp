package com.elvaco.mvp.generator;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;

import static java.util.stream.Collectors.toList;

public class MeterPopulationSpecification {

  private final Random random;
  private Set<MeterDefinition> definitions;
  private int count;
  private double lossFactor;
  private ZonedDateTime start;
  private ZonedDateTime end;
  private Organisation organisation;
  private Duration reportInterval;

  public MeterPopulationSpecification(long seed) {
    this.random = new Random(seed);
  }

  public MeterPopulationSpecification withDefinitionsFrom(Set<MeterDefinition> definitions) {
    this.definitions = definitions;
    return this;
  }

  public List<GeneratedData> create() {
    return IntStream.range(0, count)
      .mapToObj(g -> toGeneratedData())
      .collect(toList());
  }

  public MeterPopulationSpecification withMeterCount(int count) {
    this.count = count;
    return this;
  }

  public MeterPopulationSpecification withMeasurementLossFactor(double lossFactor) {
    this.lossFactor = lossFactor;
    return this;
  }

  public MeterPopulationSpecification withMeasurementsBetween(
    ZonedDateTime start,
    ZonedDateTime end
  ) {
    this.start = start;
    this.end = end;
    return this;
  }

  public MeterPopulationSpecification withOrganisation(Organisation organisation) {
    this.organisation = organisation;
    return this;
  }

  public MeterPopulationSpecification withReportInterval(Duration reportInterval) {
    this.reportInterval = reportInterval;
    return this;
  }

  private MeterDefinition pickDefinition() {
    return new ArrayList<>(definitions).get(random.nextInt(definitions.size()));
  }

  private GeneratedData toGeneratedData() {
    return new LogicalMeterSpecification(random)
      .withDefinition(pickDefinition())
      .withMeasurementLossFactor(lossFactor)
      .withMeasurementsBetween(start, end)
      .withOrganisation(organisation)
      .withReportInterval(reportInterval)
      .create();
  }
}
