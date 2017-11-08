import {schema} from 'normalizr';

const city = new schema.Entity('cities');
const address = new schema.Entity('addresses');
const addressCluster = new schema.Entity('addressClusters');

export const geoDataSchema = {
  cities: [city],
  addresses: [address],
};

export const sidebarTreeSchema = {
  cities: [city],
  addresses: [address],
  addressClusters: [addressCluster],
};
