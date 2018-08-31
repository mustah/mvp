import {normalize, schema, Schema} from 'normalizr';
import {Normalized} from '../../domain-models/domainModels';
import {DataFormatter} from '../../domain-models/domainModelsActions';
import {MeterDetails} from '../../domain-models/meter-details/meterDetailsModels';
import {MeterStatusChangelog} from '../meter/meterModels';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {Gateway, GatewayStatusChangelog} from './gatewayModels';

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
const gatewaySchema: Schema = {content: [gateway]};

const statusChangelog = new schema.Entity('statusChangelog');
const statusChangelogSchema: Schema = {statusChangelog: [statusChangelog]};

type Changelogs = GatewayStatusChangelog | MeterStatusChangelog;

export const statusChangelogDataFormatter: DataFormatter<Normalized<Changelogs>> =
  (domainModel: Gateway | MeterDetails): Normalized<Changelogs> => {
    const {entities, result} = normalize(domainModel, statusChangelogSchema);
    return {
      entities: entities.statusChangelog,
      result: Array.isArray(result.statusChangelog) ? result.statusChangelog : [],
    };
  };

export const gatewayDataFormatter: DataFormatter<NormalizedPaginated<Gateway>> =
  (response) => normalize(response, gatewaySchema) as NormalizedPaginated<Gateway>;
