import {normalize, Schema, schema} from 'normalizr';
import {DataFormatter} from '../../domain-models/domainModelsActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {Meter} from './meterModels';

export const meterProcessStrategy = (entity: any): schema.StrategyFunction => {
  if (entity.status) {
    const statusCode = entity.status.toLowerCase();
    const status = {id: statusCode, name: statusCode};
    return {
      ...entity,
      status,
    };
  }
  return entity;
};
const meter: Schema = new schema.Entity('meters', {}, {processStrategy: meterProcessStrategy});
const meterSchema: Schema = {content: [meter]};

export const measurement = [new schema.Entity('measurements', {}, {idAttribute: 'quantity'})];

export const meterDataFormatter: DataFormatter<NormalizedPaginated<Meter>> =
  (response) => normalize(response, meterSchema) as NormalizedPaginated<Meter>;
