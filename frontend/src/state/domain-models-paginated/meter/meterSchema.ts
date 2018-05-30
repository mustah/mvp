import {Schema, schema} from 'normalizr';

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

export const meterSchema = {content: [meter]};

export const measurement = [new schema.Entity('measurements', {}, {idAttribute: 'quantity'})];
