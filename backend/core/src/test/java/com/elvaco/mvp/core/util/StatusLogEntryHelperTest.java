package com.elvaco.mvp.core.util;

import java.time.ZonedDateTime;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import org.junit.Test;

import static com.elvaco.mvp.core.util.StatusLogEntryHelper.replaceActiveStatus;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class StatusLogEntryHelperTest {

  @Test
  public void firstStatus() {
    ZonedDateTime now = ZonedDateTime.now();
    assertThat(replaceActiveStatus(
      emptyList(),
      new StatusLogEntry<>(0L, 0, StatusType.OK, now, null)
    )).containsExactly(
      new StatusLogEntry<>(0L, 0, StatusType.OK, now, null)
    );
  }

  @Test
  public void replacesDifferentStatus() {
    ZonedDateTime now = ZonedDateTime.now();
    assertThat(replaceActiveStatus(
      singletonList(
        new StatusLogEntry<>(0L, 0, StatusType.OK, now, null)
      ),
      new StatusLogEntry<>(1L, 0, StatusType.ERROR, now.plusMinutes(1), null)
    )).containsExactly(
      new StatusLogEntry<>(0L, 0, StatusType.OK, now, now.plusMinutes(1)),
      new StatusLogEntry<>(1L, 0, StatusType.ERROR, now.plusMinutes(1), null)
    );
  }

  @Test
  public void doesNotReplaceSameStatus() {
    ZonedDateTime now = ZonedDateTime.now();
    assertThat(replaceActiveStatus(
      singletonList(
        new StatusLogEntry<>(0L, 0, StatusType.OK, now, null)
      ),
      new StatusLogEntry<>(1L, 0, StatusType.OK, now.plusMinutes(1), null)
    )).containsExactly(
      new StatusLogEntry<>(0L, 0, StatusType.OK, now, null)
    );
  }
}
