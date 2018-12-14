package com.elvaco.mvp.database;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.database.entity.jooq.tables.records.PhysicalMeterRecord;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.database.entity.jooq.Tables.ORGANISATION;
import static com.elvaco.mvp.database.entity.jooq.Tables.PHYSICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.MvpUser.MVP_USER;
import static com.elvaco.mvp.database.repository.jooq.CustomConditions.periodContains;
import static com.elvaco.mvp.database.repository.jooq.CustomConditions.periodOverlaps;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class JooqTest extends IntegrationTest {

  private static final ZonedDateTime DATE_TIME = ZonedDateTime.parse("2018-01-01T01:00:00+01");

  @Autowired
  private DSLContext dsl;

  @Transactional
  @Test
  public void transactional() {
    boolean rollback = false;
    try {
      UUID orgId = randomUUID();

      dsl.insertInto(ORGANISATION)
        .set(ORGANISATION.ID, orgId)
        .set(ORGANISATION.NAME, "org-1")
        .set(ORGANISATION.SLUG, "org-1")
        .set(ORGANISATION.EXTERNAL_ID, "org-1")
        .execute();

      dsl.insertInto(MVP_USER)
        .set(MVP_USER.ID, randomUUID())
        .set(MVP_USER.NAME, "tester")
        .set(MVP_USER.EMAIL, "tester@tester.com")
        .set(MVP_USER.PASSWORD, "tester")
        .set(MVP_USER.ORGANISATION_ID, randomUUID())
        .set(MVP_USER.LANGUAGE, Language.en.name())
        .execute();

      fail();
    } catch (DataIntegrityViolationException ignore) {
      rollback = true;
    }

    assertThat(rollback).isTrue();
  }

  @Test
  public void tstzrangeBindingWorksCorrectly() {
    try {
      UUID meterId = insertMeterWithActivePeriod(PeriodRange.halfOpenFrom(
        DATE_TIME,
        DATE_TIME.plusDays(1)
      ));
      PeriodRange activePeriod = fetchMeterField(PHYSICAL_METER.ACTIVE_PERIOD, meterId);

      assertThat(activePeriod)
        .isEqualTo(PeriodRange.halfOpenFrom(DATE_TIME, DATE_TIME.plusDays(1)));
    } finally {
      dsl.delete(PHYSICAL_METER).execute();
    }
  }

  @Test
  public void emptyTstzrangeBindingWorksCorrectly() {
    try {
      UUID meterId = insertMeterWithActivePeriod(PeriodRange.empty());
      PeriodRange activePeriod = fetchMeterField(PHYSICAL_METER.ACTIVE_PERIOD, meterId);

      assertThat(activePeriod)
        .isEqualTo(PeriodRange.empty());
    } finally {
      dsl.delete(PHYSICAL_METER).execute();
    }
  }

  @Test
  public void implicitEmptyTstzrangeBindingWorksCorrectly() {
    try {
      UUID meterId = insertMeterWithActivePeriod(PeriodRange.halfOpenFrom(DATE_TIME, DATE_TIME));
      PeriodRange activePeriod = fetchMeterField(PHYSICAL_METER.ACTIVE_PERIOD, meterId);

      assertThat(activePeriod)
        .isEqualTo(PeriodRange.empty());
    } finally {
      dsl.delete(PHYSICAL_METER).execute();
    }
  }

  @Test
  public void periodContainsTime() {
    try {
      UUID meterId = insertMeterWithActivePeriod(PeriodRange.halfOpenFrom(
        DATE_TIME,
        DATE_TIME.plusDays(1)
      ));
      UUID selectedId = fetchMeterField(PHYSICAL_METER.ID, meterId,
        periodContains(PHYSICAL_METER.ACTIVE_PERIOD, DATE_TIME.toOffsetDateTime())
      );

      assertThat(selectedId).isEqualTo(meterId);

      selectedId = fetchMeterField(PHYSICAL_METER.ID, meterId,
        periodContains(
          PHYSICAL_METER.ACTIVE_PERIOD,
          DATE_TIME.minusMinutes(1).toOffsetDateTime()
        )
      );

      assertThat(selectedId).isNull();
    } finally {
      dsl.delete(PHYSICAL_METER).execute();
    }
  }

  @Test
  public void periodOverlapsOtherPeriod() {
    try {
      UUID meterId = insertMeterWithActivePeriod(PeriodRange.halfOpenFrom(
        DATE_TIME,
        DATE_TIME.plusDays(1)
      ));

      UUID selectedId = fetchMeterField(PHYSICAL_METER.ID, meterId,
        periodOverlaps(
          PHYSICAL_METER.ACTIVE_PERIOD,
          PeriodRange.halfOpenFrom(DATE_TIME.minusHours(1), DATE_TIME.plusHours(1))
        )
      );

      assertThat(selectedId).isEqualTo(meterId);

      selectedId = fetchMeterField(PHYSICAL_METER.ID, meterId,
        periodOverlaps(
          PHYSICAL_METER.ACTIVE_PERIOD,
          PeriodRange.halfOpenFrom(DATE_TIME.plusDays(2), DATE_TIME.plusDays(4))
        )
      );

      assertThat(selectedId).isNull();
    } finally {
      dsl.delete(PHYSICAL_METER).execute();
    }
  }

  private <T> T fetchMeterField(
    TableField<PhysicalMeterRecord, T> field,
    UUID meterId,
    Condition... extraConditions
  ) {
    return dsl.select(field)
      .from(PHYSICAL_METER)
      .where(DSL.and(extraConditions).and(PHYSICAL_METER.ID.equal(meterId)))
      .fetchOne(0, field.getType());
  }

  private UUID insertMeterWithActivePeriod(PeriodRange periodRange) {
    UUID meterId = randomUUID();
    dsl.insertInto(PHYSICAL_METER)
      .set(PHYSICAL_METER.ID, meterId)
      .set(PHYSICAL_METER.EXTERNAL_ID, "external-id")
      .set(PHYSICAL_METER.ORGANISATION_ID, context().organisationId())
      .set(PHYSICAL_METER.ACTIVE_PERIOD, periodRange)
      .set(PHYSICAL_METER.ADDRESS, "123456789")
      .set(PHYSICAL_METER.READ_INTERVAL_MINUTES, 60L)
      .execute();
    return meterId;
  }
}
