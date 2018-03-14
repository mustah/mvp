import {schema} from 'normalizr';
import {processStrategy} from '../../domain-models/selections/selectionsSchemas';

const meter = new schema.Entity('meters', {}, {processStrategy});
const address = new schema.Entity('addresses');
const city = new schema.Entity('cities');
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
