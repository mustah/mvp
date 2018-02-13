import {schema} from 'normalizr';
import {address, city} from '../../domain-models/domainModelsSchemas';

const meter = new schema.Entity('meters');
const metersAll = new schema.Entity('metersAll');
export const meterSchema = {content: [meter]};
export const metersAllSchema = [metersAll];
export const addressCluster = new schema.Entity('addressClusters');

export const selectionTreeSchema = {
  cities: [city],
  addresses: [address],
  addressClusters: [addressCluster],
  meters: [meter],
};
