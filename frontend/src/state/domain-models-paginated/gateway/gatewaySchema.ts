import {normalize, schema, Schema} from 'normalizr';
import {DataFormatter} from '../../domain-models/domainModelsActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {Gateway} from './gatewayModels';

const processStrategy = (entity): schema.StrategyFunction<Gateway> => {
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

export const gatewayDataFormatter: DataFormatter<NormalizedPaginated<Gateway>> =
  (response) => normalize(response, gatewaySchema) as NormalizedPaginated<Gateway>;
