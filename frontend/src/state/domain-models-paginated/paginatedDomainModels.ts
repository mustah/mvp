import {Dictionary, ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {ObjectsById} from '../domain-models/domainModels';
import {SortingOptions} from '../ui/pagination/paginationModels';
import {GatewaysState} from './gateway/gatewayModels';
import {Meter} from './meter/meterModels';

export interface PaginatedDomainModelsState {
  meters: NormalizedPaginatedState<Meter>;
  gateways: GatewaysState;
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

export interface NormalizedPaginated<T extends Identifiable> extends HasPageNumber {
  entities: {[entityType: string]: ObjectsById<T>};
  result: NormalizedPaginatedResult;
}

export type SingleEntityFailure = Identifiable & ErrorResponse;

export interface NormalizedPaginatedState<T extends Identifiable = Identifiable> {
  isFetchingSingle: boolean;
  nonExistingSingles: Dictionary<SingleEntityFailure>;
  entities: ObjectsById<T>;
  result: {
    [page: number]: PaginatedResult;
  };
}

interface PaginatedResult {
  isFetching: boolean;
  isSuccessfullyFetched: boolean;
  result?: uuid[];
  error?: ErrorResponse;
}
