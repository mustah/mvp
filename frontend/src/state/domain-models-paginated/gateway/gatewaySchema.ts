import {schema, Schema} from 'normalizr';

const processStrategy = (entity): schema.StrategyFunction => {
  if (entity.status) {
    const statusCode = entity.status.toLowerCase();
    const status = {id: statusCode, name: statusCode};
    return {...entity, status};
  } else {
    return entity;
  }
};

const gateway = new schema.Entity('gateways', {}, {processStrategy});
export const gatewaySchema: Schema = {content: [gateway]};

const statusChangelog = new schema.Entity('statusChangelog');
export const statusChangelogSchema: Schema = {statusChangelog: [statusChangelog]};
