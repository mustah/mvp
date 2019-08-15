import {normalize, Schema, schema} from 'normalizr';
import {toIdNamed} from '../../../types/Types';
import {Normalized} from '../../domain-models/domainModels';
import {DataFormatter} from '../../domain-models/domainModelsActions';
import {MeterDetails} from '../../domain-models/meter-details/meterDetailsModels';
import {Gateway} from '../gateway/gatewayModels';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {EventLog, Meter} from './meterModels';

const toGateway = (gateway?: any): Gateway | undefined =>
  gateway
    ? {...gateway, status: toIdNamed(gateway.status.toLowerCase())}
    : undefined;

export const meterProcessStrategy = (entity: any): schema.StrategyFunction<Meter> => ({
  ...entity,
  gateway: toGateway(entity.gateway),
});
const meter: Schema = new schema.Entity('meters', {}, {processStrategy: meterProcessStrategy});
const meterSchema: Schema = {content: [meter]};

const eventLog = new schema.Entity('eventLog', {}, {
  idAttribute: value => `${value.start}_${value.type}`
});
const eventLogSchema: Schema = {eventLog: [eventLog]};

export const measurement = [new schema.Entity('measurements', {}, {idAttribute: 'quantity'})];

export const meterDataFormatter: DataFormatter<NormalizedPaginated<Meter>> =
  (response) => normalize(response, meterSchema) as NormalizedPaginated<Meter>;

export const eventsDataFormatter: DataFormatter<Normalized<EventLog>> =
  (domainModel: MeterDetails): Normalized<EventLog> => {
    const {entities, result} = normalize(domainModel, eventLogSchema);
    return {
      entities: entities.eventLog,
      result: Array.isArray(result.eventLog) ? result.eventLog : [],
    };
  };
