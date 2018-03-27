package com.elvaco.mvp.generator;

import javax.annotation.Nullable;

@FunctionalInterface
interface QuantitySeriesGenerator {

  double next(@Nullable Double lastValue);
}
