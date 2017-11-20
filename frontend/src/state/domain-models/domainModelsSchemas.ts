import {schema, Schema} from 'normalizr';

export const city = new schema.Entity('cities');
export const address = new schema.Entity('addresses');
const alarm = new schema.Entity('alarms');
const manufacturer = new schema.Entity('manufacturers');
const productModel = new schema.Entity('productModels');
const status = new schema.Entity('statuses');
const meteringPoint = new schema.Entity('meteringPoints');

export const selectionsSchema: Schema = {
  cities: [city],
  addresses: [address],
  alarms: [alarm],
  manufacturers: [manufacturer],
  productModels: [productModel],
  statuses: [status],
  meteringPoints: [meteringPoint],
};
