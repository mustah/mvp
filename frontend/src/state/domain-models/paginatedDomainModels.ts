import {ErrorResponse, HasId, uuid} from '../../types/Types';
import {SortingOptions} from '../ui/pagination/paginationModels';
import {ObjectsById} from './domainModels';

export interface PaginationMetadata {
  content: uuid[];
  first: boolean;
  last: boolean;
  number: number;
  numberOfElements: number;
  size: number;
  sort: SortingOptions[] | null;
  totalElements: number;
  totalPages: number;
}

export interface HasComponentId {
  componentId: uuid;
}

export interface NormalizedPaginated<T extends HasId> extends HasComponentId {
  entities: {[entityType: string]: ObjectsById<T>};
  result: PaginationMetadata;
}

export interface NormalizedPaginatedState<T extends HasId = HasId> {
  entities: ObjectsById<T>;
  result: {[componentId: string]: PaginatedResult};
}

interface PaginatedResult extends PaginationMetadata {
  isFetching: boolean;
  error?: ErrorResponse;
}
