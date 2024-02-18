package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

import lombok.Builder;

@Builder
public record AlarmDescriptionMbusQuery(String manufacturer, @Nullable Integer deviceType, int version) {}
