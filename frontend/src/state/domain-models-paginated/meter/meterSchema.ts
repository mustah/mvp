import {schema} from 'normalizr';

export const meterProcessStrategy = (entity: any): schema.StrategyFunction => {
  if (entity.status) {
    const statusCode = entity.status.toLowerCase();
    const status = {id: statusCode, name: statusCode};
    const gatewayStatus = entity.gateway.status;
    return {
      ...entity,
      status,
      gateway: {...entity.gateway, status: {id: gatewayStatus, name: gatewayStatus}},
    };
  }
  return entity;
};

const meter = new schema.Entity('meters', {}, {processStrategy: meterProcessStrategy});

export const meterSchema = {content: [meter]};
export const measurement = [new schema.Entity('measurements', {}, {idAttribute: 'quantity'})];
