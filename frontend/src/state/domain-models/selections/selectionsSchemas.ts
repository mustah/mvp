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

// TODO: Add typing to these entities
const createId: schema.SchemaFunction = (
  {name: entityName},
  {name: parentName, id: parentId},
  key,
): string => (parentId
  ? `${parentId.toLowerCase()},${entityName.toLowerCase()}`
  : `${parentName.toLowerCase()},${entityName.toLowerCase()}`);

const processStrategyCityAddress: schema.StrategyFunction = (entity, parent, key) => {
  const id = createId(entity, parent, key);
  const {name} = entity;
  return {...entity, id, name, parentId: parent.id};
};

const processStrategyCountry: schema.StrategyFunction = (entity, parent, key) => ({...entity, id: entity.name});

const alarm = new schema.Entity('alarms');
const meterStatus = new schema.Entity('meterStatuses');
const gatewayStatus = new schema.Entity('gatewayStatuses');
const address = new schema.Entity('addresses', {}, {
  idAttribute: createId,
  processStrategy: processStrategyCityAddress,
});
const city = new schema.Entity('cities', {addresses: [address]}, {
  idAttribute: createId,
  processStrategy: processStrategyCityAddress,
});
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
