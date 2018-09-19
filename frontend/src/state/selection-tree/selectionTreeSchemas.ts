import {Schema, schema} from 'normalizr';

const idOfAddress: schema.SchemaFunction = (
  {name: entityName},
  {id: parentId},
  key,
): string => `${parentId.toLowerCase()},${entityName.toLowerCase()}`;

const processStrategyAddress: schema.StrategyFunction = (entity, parent, key) => ({
  ...entity,
  id: idOfAddress(entity, parent, key),
  name: entity.name,
  city: parent.id,
});

const processStrategyMeter: schema.StrategyFunction = (entity, parent, key) => ({
  ...entity,
  id: entity.id,
  name: entity.name,
  city: parent.city,
  address: parent.name,
});

const meter = new schema.Entity('meters', {}, {
  processStrategy: processStrategyMeter,
});
const address = new schema.Entity('addresses', {meters: [meter]}, {
  idAttribute: idOfAddress,
  processStrategy: processStrategyAddress,
});
const city = new schema.Entity('cities', {addresses: [address]});

export const selectionTreeSchema: Schema = {
  cities: [city],
};
