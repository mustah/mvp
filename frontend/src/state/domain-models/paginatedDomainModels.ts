import {ErrorResponse, uuid} from '../../types/Types';
import {ObjectsById} from './domainModels';

interface SortingOptions {
  direction: 'ASC' | 'DESC';
  property: string;
  ignoreCase: boolean;
  nullHandling: string;
  ascending: boolean;
  descending: boolean;
}

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

export interface NormalizedPaginated<T> {
  entities: {[key: string]: ObjectsById<T>};
  result: PaginationMetadata;
}

export interface NormalizedPaginatedState<T> {
  entities: ObjectsById<T>;
  result: ObjectsById<PaginatedResult>;
}

interface PaginatedResult extends PaginationMetadata {
  isFetching: boolean;
  error?: ErrorResponse;
}
