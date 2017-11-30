import {schema, Schema} from 'normalizr';

const gateway = new schema.Entity('gateways');
export const gatewaySchema: Schema =  [gateway];

const statusChangelog = new schema.Entity('statusChangelog');
export const statusChangelogSchema: Schema = {statusChangelog: [statusChangelog]};
