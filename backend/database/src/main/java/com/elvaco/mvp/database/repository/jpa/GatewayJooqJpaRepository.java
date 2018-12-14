package com.elvaco.mvp.database.repository.jpa;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.dto.GatewaySummaryDto;
import com.elvaco.mvp.core.dto.LogicalMeterLocation;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.repository.jooq.JooqFilterVisitor;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record15;
import org.jooq.RecordHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.elvaco.mvp.database.entity.jooq.tables.Gateway.GATEWAY;
import static com.elvaco.mvp.database.entity.jooq.tables.GatewayStatusLog.GATEWAY_STATUS_LOG;
import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.resolveSortFields;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class GatewayJooqJpaRepository
  extends BaseQueryDslRepository<GatewayEntity, UUID>
  implements GatewayJpaRepository {

  private static final Map<String, Field<?>> SORT_FIELDS_MAP = Map.of(
    "serial", GATEWAY.SERIAL
  );

  private final DSLContext dsl;
  private final JooqFilterVisitor gatewayJooqConditions;

  @Autowired
  GatewayJooqJpaRepository(
    EntityManager entityManager,
    DSLContext dsl,
    JooqFilterVisitor gatewayJooqConditions
  ) {
    super(entityManager, GatewayEntity.class);
    this.dsl = dsl;
    this.gatewayJooqConditions = gatewayJooqConditions;
  }

  @Override
  public Optional<GatewayEntity> findById(UUID id) {
    return fetchOne(GATEWAY.ID.equal(id));
  }

  @Override
  public Page<GatewaySummaryDto> findAll(RequestParameters parameters, Pageable pageable) {
    var selectQuery = dsl.select(
      GATEWAY.ID,
      GATEWAY.ORGANISATION_ID,
      GATEWAY.SERIAL,
      GATEWAY.PRODUCT_MODEL,
      GATEWAY_STATUS_LOG.ID,
      GATEWAY_STATUS_LOG.STATUS,
      GATEWAY_STATUS_LOG.START,
      GATEWAY_STATUS_LOG.STOP,
      LOCATION.LOGICAL_METER_ID,
      LOCATION.LATITUDE,
      LOCATION.LONGITUDE,
      LOCATION.CONFIDENCE,
      LOCATION.COUNTRY,
      LOCATION.CITY,
      LOCATION.STREET_ADDRESS
    ).distinctOn(GATEWAY.ID, LOCATION.LOGICAL_METER_ID)
      .from(GATEWAY);

    var countQuery = dsl.selectDistinct(GATEWAY.ID, LOCATION.LOGICAL_METER_ID).from(GATEWAY);

    var filters = toFilters(parameters);

    gatewayJooqConditions.apply(filters, selectQuery);
    gatewayJooqConditions.apply(filters, countQuery);

    var recordHandler = new GatewaySummaryRecordHandler();

    selectQuery.limit(pageable.getPageSize())
      .offset(Long.valueOf(pageable.getOffset()).intValue())
      .fetch()
      .into(recordHandler);

    return getPage(recordHandler.getDtos(), pageable, () -> dsl.fetchCount(countQuery));
  }

  @Override
  public Page<String> findSerials(RequestParameters parameters, Pageable pageable) {
    var query = dsl.selectDistinct(GATEWAY.SERIAL).from(GATEWAY);
    var countQuery = dsl.selectDistinct(GATEWAY.SERIAL).from(GATEWAY);

    Filters filters = toFilters(parameters);
    gatewayJooqConditions.apply(filters, query);
    gatewayJooqConditions.apply(filters, countQuery);

    List<String> gatewaySerials = query
      .orderBy(resolveSortFields(parameters, SORT_FIELDS_MAP))
      .limit(pageable.getPageSize())
      .offset(Long.valueOf(pageable.getOffset()).intValue())
      .fetchInto(String.class);

    return getPage(gatewaySerials, pageable, () -> dsl.fetchCount(countQuery));
  }

  @Override
  public List<GatewayEntity> findAllByOrganisationId(UUID organisationId) {
    return nativeQuery(dsl.select().from(GATEWAY)
      .where(GATEWAY.ORGANISATION_ID.equal(organisationId)));
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndProductModelAndSerial(
    UUID organisationId,
    String productModel,
    String serial
  ) {
    return fetchOne(GATEWAY.ORGANISATION_ID.equal(organisationId)
      .and(GATEWAY.PRODUCT_MODEL.equal(productModel).and(GATEWAY.SERIAL.equal(serial))));
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndSerial(UUID organisationId, String serial) {
    return fetchOne(GATEWAY.ORGANISATION_ID.equal(organisationId)
      .and(GATEWAY.SERIAL.equal(serial)));
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return fetchOne(GATEWAY.ORGANISATION_ID.equal(organisationId).and(GATEWAY.ID.equal(id)));
  }

  private Optional<GatewayEntity> fetchOne(Condition... conditions) {
    return nativeQuery(dsl.select().from(GATEWAY).where(conditions).limit(1)).stream()
      .findAny();
  }

  private static class GatewaySummaryRecordHandler
    implements RecordHandler<Record15<UUID, UUID, String, String, Long, String,
    OffsetDateTime, OffsetDateTime, UUID, Double, Double, Double, String, String, String>> {

    private final Map<UUID, GatewaySummaryDto> gatewaySummaryDtos = new HashMap<>();

    @Override
    public void next(
      Record15<UUID, UUID, String, String, Long, String, OffsetDateTime, OffsetDateTime, UUID,
        Double, Double, Double, String, String, String> record
    ) {
      GatewaySummaryDto summaryDto = gatewaySummaryDtos.getOrDefault(
        record.value1(),
        new GatewaySummaryDto(
          record.value1(),
          record.value2(),
          record.value3(),
          record.value4(),
          record.value5(),
          StatusType.from(record.value6()),
          record.value7(),
          record.value8()
        )
      );
      summaryDto.addLocation(
        new LogicalMeterLocation(
          record.value9(),
          new com.elvaco.mvp.core.domainmodels.Location(
            record.value10(),
            record.value11(),
            record.value12(),
            record.value13(),
            record.value14(),
            record.value15()
          )
        )
      );
      gatewaySummaryDtos.put(record.value1(), summaryDto);
    }

    private List<GatewaySummaryDto> getDtos() {
      return new ArrayList<>(gatewaySummaryDtos.values());
    }
  }
}
