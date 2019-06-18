import {normalize, schema} from 'normalizr';
import {DomainModelsState, Normalized} from '../domainModels';
import {DataFormatter} from '../domainModelsActions';
import {CollectionStat} from './collectionStatModels';

export const makeDataFormatter = (entityKey: keyof DomainModelsState): DataFormatter<Normalized<CollectionStat>> =>
  (response) => normalize(response, [new schema.Entity(entityKey)]);
