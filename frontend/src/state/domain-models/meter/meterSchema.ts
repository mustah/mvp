import {schema} from 'normalizr';
import {address, addressCluster, city} from '../domainModelsSchemas';

const meter = new schema.Entity('meters');
export const meterSchema =  [meter];

export const sidebarTreeSchema = {
  cities: [city],
  addresses: [address],
  addressClusters: [addressCluster],
  meters: [meter],
};
