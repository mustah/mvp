import {normalize, Schema, schema} from 'normalizr';
import {DataFormatter} from '../domain-models/domainModelsActions';
import {getMediumType} from '../ui/graph/measurement/measurementModels';
import {NormalizedSelectionTree} from './selectionTreeModels';

const idOfAddress: schema.SchemaFunction = (
  {name: entityName},
  {id: parentId},
): string => `${parentId.toLowerCase()},${entityName.toLowerCase()}`;

const processStrategyCity: schema.StrategyFunction = (entity) => ({
  ...entity,
  medium: entity.medium ? entity.medium.map(getMediumType) : [],
});

const processStrategyAddress: schema.StrategyFunction = (entity, parent, key) => ({
  ...entity,
  id: idOfAddress(entity, parent, key),
  name: entity.name,
  city: parent.id,
});

const processStrategyMeter: schema.StrategyFunction = (entity, parent) => ({
  ...entity,
  id: entity.id,
  name: entity.name,
  city: parent.city,
  address: parent.name,
  medium: getMediumType(entity.medium),
});

const meter = new schema.Entity('meters', {}, {
  processStrategy: processStrategyMeter,
});

const address = new schema.Entity('addresses', {meters: [meter]}, {
  idAttribute: idOfAddress,
  processStrategy: processStrategyAddress,
});

const city = new schema.Entity('cities', {addresses: [address]}, {
  processStrategy: processStrategyCity,
});

const selectionTreeSchema: Schema = {cities: [city]};

export const selectionTreeDataFormatter: DataFormatter<NormalizedSelectionTree> =
  (response) => normalize(response, selectionTreeSchema);
