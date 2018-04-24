package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import lombok.experimental.UtilityClass;

import static java.util.Collections.reverse;

@UtilityClass
class MeteringMessageHelper {

  static Set<ValueDto> removeSimultaneousQuantityValues(List<ValueDto> values) {
    reverse(values); // reverse the list to preserve the *last* value in the list from each series
    Set<ValueDto> set = new TreeSet<>(MeteringMessageHelper::quantityTimestampComparator);
    set.addAll(values);
    return set;
  }

  private static int quantityTimestampComparator(ValueDto o1, ValueDto o2) {
    return o1.quantity.equals(o2.quantity) && o1.timestamp.equals(o2.timestamp)
      ? 0
      : -1;
  }
}
