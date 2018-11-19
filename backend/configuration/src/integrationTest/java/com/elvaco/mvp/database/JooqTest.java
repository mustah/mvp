package com.elvaco.mvp.database;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static com.elvaco.mvp.database.entity.jooq.Tables.ORGANISATION;
import static com.elvaco.mvp.database.entity.jooq.tables.MvpUser.MVP_USER;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

public class JooqTest extends IntegrationTest {

  @Autowired
  private DSLContext dsl;

  @Before
  public void setUp() {
    assumeTrue(isPostgresDialect());
  }

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
        .set(MVP_USER.ORGANISATION_ID, orgId)
        .set(MVP_USER.LANGUAGE, Language.en.name())
        .execute();

      fail();
    } catch (DataAccessException ignore) {
      rollback = true;
    }

    assertThat(rollback).isTrue();
  }
}
