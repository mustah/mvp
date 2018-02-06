import {schema} from 'normalizr';
import {address, city} from '../../domain-models/domainModelsSchemas';

const meter = new schema.Entity('meters');
export const meterSchema = {content: [meter]};
export const addressCluster = new schema.Entity('addressClusters');

export const selectionTreeSchema = {
  cities: [city],
  addresses: [address],
  addressClusters: [addressCluster],
  meters: [meter],
};
