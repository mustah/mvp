import {schema} from 'normalizr';
import {address, city} from '../../domain-models/domainModelsSchemas';

const meter = new schema.Entity('meters');
const allMeters = new schema.Entity('allMeters');
export const meterSchema = {content: [meter]};
export const allMetersSchema = [allMeters];
export const addressCluster = new schema.Entity('addressClusters');

export const selectionTreeSchema = {
  cities: [city],
  addresses: [address],
  addressClusters: [addressCluster],
  meters: [meter],
};
