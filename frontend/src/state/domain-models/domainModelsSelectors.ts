import {HasId, uuid} from '../../types/Types';
import {NormalizedState, ObjectsById} from './domainModels';

export const getResultDomainModels = <T extends HasId>(state: NormalizedState<T>): uuid[] => state.result;

export const getEntitiesDomainModels = <T extends HasId>(state: NormalizedState<T>): ObjectsById<T> => state.entities;
