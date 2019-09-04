import {Dictionary, ErrorResponse, Identifiable, uuid} from '../../types/Types';
import {CollectionStatFacilityState} from '../domain-models/collection-stat/collectionStatModels';
import {ObjectsById, RequestsHttp} from '../domain-models/domainModels';
import {ApiResultSortingOptions, SortOption} from '../ui/pagination/paginationModels';
import {BatchReferencesState} from './batch-references/batchReferenceModels';
import {DevicesState} from './devices/deviceModels';
import {GatewaysState} from './gateway/gatewayModels';
import {MetersState} from './meter/meterModels';

export interface PaginatedDomainModelsState {
  batchReferences: BatchReferencesState;
  devices: DevicesState;
  meters: MetersState;
  gateways: GatewaysState;
  collectionStatFacilities: CollectionStatFacilityState;
  meterCollectionStatFacilities: CollectionStatFacilityState;
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

export interface NormalizedPaginatedState<T extends Identifiable> {
  isFetchingSingle: boolean;
  entities: ObjectsById<T>;
  result: {
    [page: number]: PaginatedResult;
  };
  nonExistingSingles: Dictionary<SingleEntityFailure>;
  sort?: SortOption[];
}

export interface PaginatedResult extends RequestsHttp {
  result: uuid[];
}
