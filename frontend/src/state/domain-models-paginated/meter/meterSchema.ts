import {schema} from 'normalizr';
import {address, city} from '../../domain-models/domainModelsSchemas';

const processStrategy = (entity): schema.StrategyFunction => {
  if (entity.status) {
    const statusCode = entity.status.toLowerCase();
    const status = {id: statusCode, name: statusCode};
    return {...entity, status};
  } else {
    return entity;
  }
};

const meter = new schema.Entity('meters', {}, {processStrategy});
const allMeters = new schema.Entity('allMeters', {}, {processStrategy});
export const meterSchema = {content: [meter]};
export const allMetersSchema = [allMeters];
export const addressCluster = new schema.Entity('addressClusters');

export const selectionTreeSchema = {
  cities: [city],
  addresses: [address],
  addressClusters: [addressCluster],
  meters: [meter],
};
