import {schema, Schema} from 'normalizr';
import {processStrategy} from '../domainModelsSchemas';

const gateway = new schema.Entity('gateways', {}, {processStrategy});
export const gatewaySchema: Schema = [gateway];

const statusChangelog = new schema.Entity('statusChangelog');
export const statusChangelogSchema: Schema = {statusChangelog: [statusChangelog]};
