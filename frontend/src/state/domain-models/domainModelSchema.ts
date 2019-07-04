import {normalize, Schema, schema} from 'normalizr';
import {Identifiable} from '../../types/Types';
import {DomainModelsState, Normalized} from './domainModels';
import {DataFormatter} from './domainModelsActions';

export const makeDataFormatter =
  <T extends Identifiable>(
    entityKey: keyof DomainModelsState,
    definition?: Schema,
    options?: schema.EntityOptions
  ): DataFormatter<Normalized<T>> =>
    (response) => normalize(response, [new schema.Entity(entityKey, definition, options)]);
