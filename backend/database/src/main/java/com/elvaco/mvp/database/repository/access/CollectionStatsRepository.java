package com.elvaco.mvp.database.repository.access;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import com.elvaco.mvp.adapters.spring.PageAdapter;
import com.elvaco.mvp.core.dto.CollectionStatsDto;
import com.elvaco.mvp.core.dto.CollectionStatsPerDateDto;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.CollectionStats;
import com.elvaco.mvp.core.util.MeasurementThresholdParser;
import com.elvaco.mvp.database.repository.jooq.FilterVisitors;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.data.domain.PageRequest;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.elvaco.mvp.database.entity.jooq.tables.LogicalMeter.LOGICAL_METER;
import static com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeter.PHYSICAL_METER;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.COLLECTION_PERCENTAGE;
import static com.elvaco.mvp.database.repository.jooq.JooqUtils.LAST_DATA;
import static com.elvaco.mvp.database.repository.mappers.SortMapper.getAsSpringSort;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.resolveSortFields;
import static org.jooq.impl.DSL.avg;
import static org.jooq.impl.DSL.coalesce;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.when;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@RequiredArgsConstructor
public class CollectionStatsRepository implements CollectionStats {

  private static final Map<String, Field<?>> COLLECTION_SORT_FIELDS_MAP = Map.of(
    "facility", LOGICAL_METER.EXTERNAL_ID,
    "collectionPercentage", field("collectionPercentage2", Double.class),
    "lastData", LAST_DATA
  );

  private static final Field<LocalDate> GENDATE_FIELD = field("gendate", LocalDate.class);

  private final DSLContext dsl;
  private final MeasurementThresholdParser measurementThresholdParser;

  @Override
  public List<CollectionStatsDto> findAll(RequestParameters parameters, Pageable pageable) {
    return findAllPaged(parameters, pageable).getContent();
  }

  @Override
  public Page<CollectionStatsDto> findAllPaged(RequestParameters parameters, Pageable pageable) {
    var selectQuery = dsl.select(
      LOGICAL_METER.ID,
      LOGICAL_METER.EXTERNAL_ID,
      PHYSICAL_METER.READ_INTERVAL_MINUTES,
      when(PHYSICAL_METER.READ_INTERVAL_MINUTES.ne(0L), coalesce(COLLECTION_PERCENTAGE, 0.0))
        .otherwise(DSL.inline((Double) null)).as("collectionPercentage2"),
      LAST_DATA
    ).from(LOGICAL_METER);

    var countQuery = dsl.selectDistinct(LOGICAL_METER.ID, PHYSICAL_METER.ID).from(LOGICAL_METER);

    FilterVisitors.logicalMeterWithCollectionPercentageAndLastData(dsl, measurementThresholdParser)
      .accept(toFilters(parameters))
      .andJoinsOn(selectQuery)
      .andJoinsOn(countQuery);

    var select = selectQuery.orderBy(
      resolveSortFields(
        parameters,
        COLLECTION_SORT_FIELDS_MAP,
        LOGICAL_METER.EXTERNAL_ID.asc()
      ))
      .limit(pageable.getPageSize())
      .offset((int) pageable.getOffset());

    List<CollectionStatsDto> result = select.fetch().stream()
      .map(record -> new CollectionStatsDto(
        record.value1(),
        record.value2(),
        record.value3() == null ? null : record.value3().intValue(),
        record.value4(),
        record.value5()
      ))
      .toList();

    PageRequest pageRequest = PageRequest.of(
      pageable.getPageNumber(),
      pageable.getPageSize(),
      getAsSpringSort(pageable.getSort())
    );

    return new PageAdapter<>(getPage(result, pageRequest, () -> dsl.fetchCount(countQuery)));
  }

  @Override
  public List<CollectionStatsPerDateDto> findAllPerDate(RequestParameters parameters) {
    var selectQuery = dsl.select(
      avg(field("actual", Double.class).divide(field("expected", Double.class))).times(100.0),
      GENDATE_FIELD
    ).from(LOGICAL_METER);

    FilterVisitors.collectionPercentagePerDate(dsl, measurementThresholdParser)
      .accept(toFilters(parameters))
      .andJoinsOn(selectQuery);

    var select = selectQuery.groupBy(GENDATE_FIELD)
      .orderBy(GENDATE_FIELD.asc());

    return select.fetch().stream()
      .map(record -> new CollectionStatsPerDateDto(
        record.value2().atStartOfDay(ZoneId.of("UTC+1")),
        record.value1().doubleValue()
      ))
      .toList();
  }
}
