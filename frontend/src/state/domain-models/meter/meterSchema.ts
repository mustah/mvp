import {schema} from 'normalizr';
import {address, addressCluster, city} from '../geoData/geoDataSchemas';

const meter = new schema.Entity('meters');
export const meterSchema =  [meter];

export const sidebarTreeSchema = {
  cities: [city],
  addresses: [address],
  addressClusters: [addressCluster],
};
