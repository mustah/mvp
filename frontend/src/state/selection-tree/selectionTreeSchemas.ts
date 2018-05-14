import {Schema, schema} from 'normalizr';

const createId: schema.SchemaFunction = (
  {name: entityName},
  {id: parentId},
  key,
): string => (`${parentId.toLowerCase()},${entityName.toLowerCase()}`);

const processStrategyAddress: schema.StrategyFunction = (entity, parent, key) => {
  const id = createId(entity, parent, key);
  const {name} = entity;
  return {...entity, id, name};
};

const meter = new schema.Entity('meters');
const address = new schema.Entity('addresses', {meters: [meter]}, {
  idAttribute: createId,
  processStrategy: processStrategyAddress,
});
const city = new schema.Entity('cities', {addresses: [address]});

export const selectionTreeSchema: Schema = {
  cities: [city],
};
