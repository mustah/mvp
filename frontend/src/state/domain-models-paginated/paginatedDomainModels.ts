import {Dictionary, ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {Entities, ObjectsById} from '../domain-models/domainModels';
import {ApiRequestSortingOptions, ApiResultSortingOptions} from '../ui/pagination/paginationModels';
import {GatewaysState} from './gateway/gatewayModels';
import {MetersState} from './meter/meterModels';

export interface PaginatedDomainModelsState {
  meters: MetersState;
  gateways: GatewaysState;
}

export interface NormalizedPaginatedResult {
  content: uuid[];
  first?: boolean;
  last?: boolean;
  number?: number;
  numberOfElements?: number;
  size?: number;
  sort?: ApiResultSortingOptions[] | null;
  totalElements: number;
  totalPages: number;
}

export interface PageNumbered {
  page: number;
}

export interface NormalizedPaginated<T extends Identifiable> extends PageNumbered {
  entities: {[entityType: string]: ObjectsById<T>};
  result: NormalizedPaginatedResult;
}

export type SingleEntityFailure = Identifiable & ErrorResponse;

export interface NormalizedPaginatedState<T extends Identifiable = Identifiable> extends Entities<T> {
  isFetchingSingle: boolean;
  nonExistingSingles: Dictionary<SingleEntityFailure>;
  sort?: ApiRequestSortingOptions[];
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
