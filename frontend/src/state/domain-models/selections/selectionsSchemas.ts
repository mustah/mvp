import {schema, Schema} from 'normalizr';

const createId: schema.SchemaFunction = (
  {name: entityName},
  {name: parentName, id: parentId},
): string => (parentId
  ? `${parentId.toLowerCase()},${entityName.toLowerCase()}`
  : `${parentName.toLowerCase()},${entityName.toLowerCase()}`);

const processStrategyCityAddress: schema.StrategyFunction = (entity, parent, key) => {
  const id = createId(entity, parent, key);
  const {name} = entity;
  return {...entity, id, name, parentId: parent.id};
};

const processStrategyCountry: schema.StrategyFunction = (entity) => ({...entity, id: entity.name});

const alarm = new schema.Entity('alarms');
const meterStatus = new schema.Entity('meterStatuses');
const gatewayStatus = new schema.Entity('gatewayStatuses');

const options: schema.EntityOptions = {
  idAttribute: createId,
  processStrategy: processStrategyCityAddress,
};

const address = new schema.Entity('addresses', {}, options);
const city = new schema.Entity('cities', {addresses: [address]}, options);
const country = new schema.Entity('countries', {cities: [city]}, {
  idAttribute: 'name',
  processStrategy: processStrategyCountry,
});

export const selectionsSchema: Schema = {
  locations: {countries: [country]},
  alarms: [alarm],
  meterStatuses: [meterStatus],
  gatewayStatuses: [gatewayStatus],
};
