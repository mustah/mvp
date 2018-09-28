package com.elvaco.mvp.core.util;

import java.time.ZonedDateTime;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.StatusType.ERROR;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.util.StatusLogEntryHelper.replaceActiveStatus;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class StatusLogEntryHelperTest {

  @Test
  public void firstStatus() {
    ZonedDateTime now = ZonedDateTime.now();

    List<StatusLogEntry<Long>> statuses = replaceActiveStatus(
      emptyList(),
      statusBuilder().start(now).build()
    );

    assertThat(statuses).containsExactly(statusBuilder().start(now).build());
  }

  @Test
  public void replacesDifferentStatus() {
    ZonedDateTime now = ZonedDateTime.now();
    assertThat(replaceActiveStatus(
      singletonList(statusBuilder().start(now).build()),
      statusBuilder().id(1L).status(ERROR).start(now.plusMinutes(1)).build()
    )).containsExactly(
      statusBuilder().start(now).stop(now.plusMinutes(1)).build(),
      statusBuilder().id(1L).status(ERROR).start(now.plusMinutes(1)).build()
    );
  }

  @Test
  public void doesNotReplaceSameStatus() {
    ZonedDateTime now = ZonedDateTime.now();
    assertThat(replaceActiveStatus(
      singletonList(statusBuilder().start(now).build()),
      statusBuilder().id(1L).status(OK).start(now.plusMinutes(1)).build()
    )).containsExactly(statusBuilder().start(now).build());
  }

  private static StatusLogEntry.StatusLogEntryBuilder<Long> statusBuilder() {
    return StatusLogEntry.<Long>builder().id(0L).entityId(0L).status(OK);
  }
}
