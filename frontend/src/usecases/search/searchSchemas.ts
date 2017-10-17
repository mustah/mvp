import {schema} from 'normalizr';

const city = new schema.Entity('cities');
const address = new schema.Entity('addresses');
const status = new schema.Entity('statuses');
const meteringPoint = new schema.Entity('meteringPoints');

export const searchOptionsSchema = {
  cities: [city],
  addresses: [address],
  statuses: [status],
  meteringPoints: [meteringPoint],
};
