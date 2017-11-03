import {schema} from 'normalizr';

const city = new schema.Entity('cities');
const address = new schema.Entity('addresses');

export const geoDataSchema = {
  cities: [city],
  addresses: [address],
};
