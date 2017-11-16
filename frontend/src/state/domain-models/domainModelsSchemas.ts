import {schema, Schema} from 'normalizr';

export const city = new schema.Entity('cities');
export const address = new schema.Entity('addresses');
const alarm = new schema.Entity('alarms');
const manufacturer = new schema.Entity('manufacturers');
const status = new schema.Entity('statuses');
const meteringPoint = new schema.Entity('meteringPoints');

export const selectionsSchema: Schema = {
  cities: [city],
  addresses: [address],
  alarms: [alarm],
  manufacturers: [manufacturer],
  statuses: [status],
  meteringPoints: [meteringPoint],
};
