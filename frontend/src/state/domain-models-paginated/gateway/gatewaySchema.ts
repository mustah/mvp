import {schema, Schema} from 'normalizr';
import {processStrategy} from '../../domain-models/selections/selectionsSchemas';

const gateway = new schema.Entity('gateways', {}, {processStrategy});
export const gatewaySchema: Schema = {content: [gateway]};

const statusChangelog = new schema.Entity('statusChangelog');
export const statusChangelogSchema: Schema = {statusChangelog: [statusChangelog]};
