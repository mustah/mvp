import {ErrorResponse, HasId, uuid} from '../../types/Types';
import {ObjectsById} from '../domain-models/domainModels';
import {SortingOptions} from '../ui/pagination/paginationModels';
import {Meter} from './meter/meterModels';

export interface PaginatedDomainModelsState {
  meters: NormalizedPaginatedState<Meter>;
}

export interface NormalizedPaginatedResult {
  content: uuid[];
  first?: boolean;
  last?: boolean;
  number?: number;
  numberOfElements?: number;
  size?: number;
  sort?: SortingOptions[] | null;
  totalElements: number;
  totalPages: number;
}

export interface HasPageNumber {
  page: number;
}

export interface NormalizedPaginated<T extends HasId> extends HasPageNumber {
  entities: {[entityType: string]: ObjectsById<T>};
  result: NormalizedPaginatedResult;
}

export interface NormalizedPaginatedState<T extends HasId = HasId> {
  entities: ObjectsById<T>;
  result: {[page: number]: PaginatedResult};
}

interface PaginatedResult {
  isFetching: boolean;
  isSuccessfullyFetched: boolean;
  error?: ErrorResponse;
  result?: uuid[];
}

export type RestGetPaginated = (page: number, requestData?: string) => void;
