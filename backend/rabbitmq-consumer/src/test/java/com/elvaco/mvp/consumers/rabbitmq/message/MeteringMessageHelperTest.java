package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;

import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import org.junit.Test;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageHelper.removeSimultaneousQuantityValues;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class MeteringMessageHelperTest {

  @Test
  public void removeSimultaneousQuantityValues_emptyList() {
    assertThat(removeSimultaneousQuantityValues(emptyList())).isEqualTo(emptySet());
  }

  @Test
  public void removeSimultaneousQuantityValues_oneValue() {
    LocalDateTime now = LocalDateTime.now();
    assertThat(removeSimultaneousQuantityValues(singletonList(new ValueDto(
        now,
        1.0,
        "kWh",
        "Energy"
      )
    ))).containsExactly(
      new ValueDto(
        now,
        1.0,
        "kWh",
        "Energy"
      )
    );
  }

  @Test
  public void removeSimultaneousQuantityValues_noSimultaneous() {
    LocalDateTime now = LocalDateTime.now();
    assertThat(removeSimultaneousQuantityValues(asList(
      new ValueDto(
        now,
        1.0,
        "kWh",
        "Energy"
      ),
      new ValueDto(
        now.plusDays(2),
        3.0,
        "kWh",
        "Energy"
      )

    ))).containsExactly(new ValueDto(
        now,
        1.0,
        "kWh",
        "Energy"
      ), new ValueDto(
        now.plusDays(2),
        3.0,
        "kWh",
        "Energy"
      )
    );
  }

  @Test
  public void removeSimultaneousQuantityValues_oneIdenticalDuplicate() {
    LocalDateTime now = LocalDateTime.now();
    assertThat(removeSimultaneousQuantityValues(asList(
      new ValueDto(
        now,
        1.0,
        "kWh",
        "Energy"
      ),
      new ValueDto(
        now,
        1.0,
        "kWh",
        "Energy"
      )
    ))).containsExactly(
      new ValueDto(
        now,
        1.0,
        "kWh",
        "Energy"
      )
    );
  }

  @Test
  public void removeSimultaneousQuantityValues_oneSimultaneousWhereValueDiffers() {
    LocalDateTime now = LocalDateTime.now();
    assertThat(removeSimultaneousQuantityValues(asList(
      new ValueDto(
        now,
        1.0,
        "kWh",
        "Energy"
      ),
      new ValueDto(
        now,
        2.0,
        "kWh",
        "Energy"
      )
    ))).containsExactly(
      new ValueDto(
        now,
        2.0,
        "kWh",
        "Energy"
      )
    );
  }

  @Test
  public void removeSimultaneousQuantityValues_oneSimultaneousWhereValueDiffers_reversed() {
    LocalDateTime now = LocalDateTime.now();
    assertThat(removeSimultaneousQuantityValues(asList(
      new ValueDto(
        now,
        2.0,
        "kWh",
        "Energy"
      ),
      new ValueDto(
        now,
        1.0,
        "kWh",
        "Energy"
      )
    ))).containsExactly(
      new ValueDto(
        now,
        1.0,
        "kWh",
        "Energy"
      )
    );
  }

  @Test
  public void removeSimultaneousQuantityValues_onlyTimestampDiffers() {
    LocalDateTime now = LocalDateTime.now();
    assertThat(removeSimultaneousQuantityValues(asList(
      new ValueDto(
        now,
        2.0,
        "kWh",
        "Energy"
      ),
      new ValueDto(
        now.plusMinutes(1),
        2.0,
        "kWh",
        "Energy"
      )
    ))).containsExactly(
      new ValueDto(
        now,
        2.0,
        "kWh",
        "Energy"
      ),
      new ValueDto(
        now.plusMinutes(1),
        2.0,
        "kWh",
        "Energy"
      )
    );
  }

  @Test
  public void removeSimultaneousQuantityValues_oneSimultaneousWhereUnitDiffers() {
    LocalDateTime now = LocalDateTime.now();
    assertThat(removeSimultaneousQuantityValues(asList(
      new ValueDto(
        now,
        2.0,
        "kWh",
        "Energy"
      ),
      new ValueDto(
        now,
        2.0,
        "kWm",
        "Energy"
      )
    ))).containsExactly(
      new ValueDto(
        now,
        2.0,
        "kWm",
        "Energy"
      )
    );
  }

  @Test
  public void removeSimultaneousQuantityValues_onlyQuantityDiffers() {
    LocalDateTime now = LocalDateTime.now();
    assertThat(removeSimultaneousQuantityValues(asList(
      new ValueDto(
        now,
        2.0,
        "kWh",
        "Energy"
      ),
      new ValueDto(
        now,
        2.0,
        "kWh",
        "Energy 2"
      )
    ))).containsExactly(
      new ValueDto(
        now,
        2.0,
        "kWh",
        "Energy"
      ),
      new ValueDto(
        now,
        2.0,
        "kWh",
        "Energy 2"
      )
    );
  }
}
