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
import com.elvaco.mvp.database.entity.jooq.tables.Gateway;
import com.elvaco.mvp.database.entity.jooq.tables.GatewayStatusLog;
import com.elvaco.mvp.database.entity.jooq.tables.Location;
import com.elvaco.mvp.database.repository.jooq.JooqFilterVisitor;

import com.querydsl.core.types.Predicate;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record15;
import org.jooq.RecordHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.resolveSortFields;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class GatewayQueryDslJpaRepository
  extends BaseQueryDslRepository<GatewayEntity, UUID>
  implements GatewayJpaRepository {

  private static final Map<String, Field<?>> SORT_FIELDS_MAP = Map.of(
    "serial", Gateway.GATEWAY.SERIAL
  );

  private final DSLContext dsl;
  private final JooqFilterVisitor gatewayJooqConditions;

  @Autowired
  GatewayQueryDslJpaRepository(
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
    var predicate = GATEWAY.pk.id.eq(id);
    return Optional.ofNullable(createQuery(predicate).select(path).fetchOne());
  }

  @Override
  public Page<GatewaySummaryDto> findAll(RequestParameters parameters, Pageable pageable) {
    var gateway = Gateway.GATEWAY;
    var location = Location.LOCATION;
    var gatewayStatusLog = GatewayStatusLog.GATEWAY_STATUS_LOG;

    var selectQuery = dsl.select(
      gateway.ID,
      gateway.ORGANISATION_ID,
      gateway.SERIAL,
      gateway.PRODUCT_MODEL,
      gatewayStatusLog.ID,
      gatewayStatusLog.STATUS,
      gatewayStatusLog.START,
      gatewayStatusLog.STOP,
      location.LOGICAL_METER_ID,
      location.LATITUDE,
      location.LONGITUDE,
      location.CONFIDENCE,
      location.COUNTRY,
      location.CITY,
      location.STREET_ADDRESS
    ).distinctOn(gateway.ID, location.LOGICAL_METER_ID)
      .from(gateway);

    var countQuery = dsl.selectDistinct(gateway.ID, location.LOGICAL_METER_ID).from(gateway);

    var filters = toFilters(parameters);

    gatewayJooqConditions.apply(filters, selectQuery);
    gatewayJooqConditions.apply(filters, countQuery);

    var recordHandler = new GatewaySummaryRecordHandler();

    selectQuery
      .limit(pageable.getPageSize())
      .offset(Long.valueOf(pageable.getOffset()).intValue())
      .fetch()
      .into(recordHandler);

    return getPage(recordHandler.getDtos(), pageable, () -> dsl.fetchCount(countQuery));
  }

  @Override
  public Page<String> findSerials(RequestParameters parameters, Pageable pageable) {
    var gateway = Gateway.GATEWAY;

    var query = dsl.selectDistinct(gateway.SERIAL).from(gateway);
    var countQuery = dsl.selectDistinct(gateway.SERIAL).from(gateway);

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
    Predicate predicate = GATEWAY.pk.organisationId.eq(organisationId);
    return createQuery(predicate).select(path).fetch();
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndProductModelAndSerial(
    UUID organisationId,
    String productModel,
    String serial
  ) {
    Predicate predicate = GATEWAY.pk.organisationId.eq(organisationId)
      .and(GATEWAY.productModel.equalsIgnoreCase(productModel))
      .and(GATEWAY.serial.equalsIgnoreCase(serial));
    return Optional.ofNullable(createQuery(predicate).select(path).fetchOne());
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndSerial(UUID organisationId, String serial) {
    Predicate predicate = GATEWAY.pk.organisationId.eq(organisationId)
      .and(GATEWAY.serial.equalsIgnoreCase(serial));
    return Optional.ofNullable(createQuery(predicate).select(path).fetchOne());
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    Predicate predicate = GATEWAY.pk.organisationId.eq(organisationId)
      .and(GATEWAY.pk.id.eq(id));
    return Optional.ofNullable(createQuery(predicate).select(path).fetchOne());
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
