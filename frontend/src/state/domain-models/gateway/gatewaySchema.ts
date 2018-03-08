import {schema, Schema} from 'normalizr';

const processStrategy = (entity): schema.StrategyFunction => {
  if (entity.status) {
    const statusCode = entity.status.toLowerCase();
    const metersStatusCode = entity.meterStatus.toLowerCase();
    const status = {id: statusCode, name: statusCode};
    const meterStatus = {id: metersStatusCode, name: metersStatusCode};
    return {...entity, status, meterStatus};
  } else {
    return entity;
  }
};

const gateway = new schema.Entity('gateways', {}, {processStrategy});
export const gatewaySchema: Schema = [gateway];

const statusChangelog = new schema.Entity('statusChangelog');
export const statusChangelogSchema: Schema = {statusChangelog: [statusChangelog]};
