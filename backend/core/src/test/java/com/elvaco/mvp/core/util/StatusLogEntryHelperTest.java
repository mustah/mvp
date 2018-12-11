package com.elvaco.mvp.core.util;

import java.time.ZonedDateTime;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.Pk;
import com.elvaco.mvp.core.domainmodels.PrimaryKey;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.util.StatusLogEntryHelper.replaceActiveStatus;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class StatusLogEntryHelperTest {

  @Test
  public void firstStatus() {
    var now = ZonedDateTime.now();
    var primaryKey = new Pk(randomUUID(), randomUUID());

    assertThat(replaceActiveStatus(
      List.of(),
      statusBuilder(primaryKey).start(now).build()
    )).containsExactly(statusBuilder(primaryKey).start(now).build());
  }

  @Test
  public void replacesDifferentStatus() {
    var now = ZonedDateTime.now();
    var primaryKey = new Pk(randomUUID(), randomUUID());

    assertThat(replaceActiveStatus(
      List.of(statusBuilder(primaryKey).start(now).build()),
      statusBuilder(primaryKey).id(1L).status(ERROR).start(now.plusMinutes(1)).build()
    )).containsExactly(
      statusBuilder(primaryKey).start(now).stop(now.plusMinutes(1)).build(),
      statusBuilder(primaryKey).id(1L).status(ERROR).start(now.plusMinutes(1)).build()
    );
  }

  @Test
  public void doesNotReplaceSameStatus() {
    var now = ZonedDateTime.now();
    var primaryKey = new Pk(randomUUID(), randomUUID());

    assertThat(replaceActiveStatus(
      List.of(statusBuilder(primaryKey).start(now).build()),
      statusBuilder(primaryKey).id(1L).status(OK).start(now.plusMinutes(1)).build()
    )).containsExactly(statusBuilder(primaryKey).start(now).build());
  }

  private static StatusLogEntry.StatusLogEntryBuilder statusBuilder(PrimaryKey primaryKey) {
    return StatusLogEntry.builder().id(0L).primaryKey(primaryKey).status(OK);
  }
}
