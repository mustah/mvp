import {schema} from 'normalizr';

export const city = new schema.Entity('cities');
export const address = new schema.Entity('addresses');
export const addressCluster = new schema.Entity('addressClusters');

export const geoDataSchema = {
  cities: [city],
  addresses: [address],
};
