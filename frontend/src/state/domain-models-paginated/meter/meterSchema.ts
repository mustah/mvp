import {normalize, Schema, schema} from 'normalizr';
import {toIdNamed} from '../../../types/Types';
import {Normalized} from '../../domain-models/domainModels';
import {DataFormatter} from '../../domain-models/domainModelsActions';
import {MeterDetails} from '../../domain-models/meter-details/meterDetailsModels';
import {Gateway, GatewayStatusChangelog} from '../gateway/gatewayModels';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {Meter, MeterStatusChangelog} from './meterModels';

const toGateway = (gateway?: any): Gateway | undefined =>
  gateway
    ? {...gateway, status: toIdNamed(gateway.status.toLowerCase())}
    : undefined;

export const meterProcessStrategy = (entity: any): schema.StrategyFunction => ({
  ...entity,
  gateway: toGateway(entity.gateway),
});
const meter: Schema = new schema.Entity('meters', {}, {processStrategy: meterProcessStrategy});
const meterSchema: Schema = {content: [meter]};

const statusChangelog = new schema.Entity('statusChangelog');
const statusChangelogSchema: Schema = {statusChangelog: [statusChangelog]};

export const measurement = [new schema.Entity('measurements', {}, {idAttribute: 'quantity'})];

type Changelogs = GatewayStatusChangelog | MeterStatusChangelog;

export const meterDataFormatter: DataFormatter<NormalizedPaginated<Meter>> =
  (response) => normalize(response, meterSchema) as NormalizedPaginated<Meter>;

export const statusChangelogDataFormatter: DataFormatter<Normalized<Changelogs>> =
  (domainModel: Gateway | MeterDetails): Normalized<Changelogs> => {
    const {entities, result} = normalize(domainModel, statusChangelogSchema);
    return {
      entities: entities.statusChangelog,
      result: Array.isArray(result.statusChangelog) ? result.statusChangelog : [],
    };
  };
