import {ErrorResponse, HasId, uuid} from '../../types/Types';
import {HasComponentId, SortingOptions} from '../ui/pagination/paginationModels';
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

export interface HasPageNumber {
  page: number;
}

export interface NormalizedPaginated<T extends HasId> extends HasPageNumber {
  entities: {[entityType: string]: ObjectsById<T>};
  result: PaginationMetadata;
}

export interface NormalizedPaginatedState<T extends HasId = HasId> {
  entities: ObjectsById<T>;
  result: {[page: number]: PaginatedResult};
}

interface PaginatedResult {
  isFetching: boolean;
  error?: ErrorResponse;
  result?: uuid[];
}
