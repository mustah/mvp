import {schema, Schema} from 'normalizr';

export const processStrategy = (entity): schema.StrategyFunction => {
  if (entity.status) {
    const statusCode = entity.status.toLowerCase();
    const status = {id: statusCode, name: statusCode};
    return {...entity, status};
  } else {
    return entity;
  }
};

export const city = new schema.Entity('cities');
export const address = new schema.Entity('addresses');
const alarm = new schema.Entity('alarms');
const manufacturer = new schema.Entity('manufacturers');
const productModel = new schema.Entity('productModels');
const meterStatus = new schema.Entity('meterStatuses');
const gatewayStatus = new schema.Entity('gatewayStatuses');
const meteringPoint = new schema.Entity('meteringPoints');

export const selectionsSchema: Schema = {
  cities: [city],
  addresses: [address],
  alarms: [alarm],
  manufacturers: [manufacturer],
  productModels: [productModel],
  meterStatuses: [meterStatus],
  gatewayStatuses: [gatewayStatus],
  meteringPoints: [meteringPoint],
};
