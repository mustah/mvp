import {normalize, Schema, schema} from 'normalizr';
import {toIdNamed} from '../../../types/Types';
import {DataFormatter} from '../../domain-models/domainModelsActions';
import {Gateway} from '../gateway/gatewayModels';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {Meter} from './meterModels';

const toGateway = (gateway?: any): Gateway | undefined =>
  gateway
    ? {...gateway, status: toIdNamed(gateway.status.toLowerCase())}
    : undefined;

export const meterProcessStrategy = (entity: any): schema.StrategyFunction => {
  if (entity.status) {
    return {
      ...entity,
      status: toIdNamed(entity.status.toLowerCase()),
      gateway: toGateway(entity.gateway),
    };
  }
  return entity;
};
const meter: Schema = new schema.Entity('meters', {}, {processStrategy: meterProcessStrategy});
const meterSchema: Schema = {content: [meter]};

export const measurement = [new schema.Entity('measurements', {}, {idAttribute: 'quantity'})];

export const meterDataFormatter: DataFormatter<NormalizedPaginated<Meter>> =
  (response) => normalize(response, meterSchema) as NormalizedPaginated<Meter>;
