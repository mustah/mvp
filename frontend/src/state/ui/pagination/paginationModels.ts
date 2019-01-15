import {RequestParameter} from '../../../helpers/urlFactory';
import {uuid} from '../../../types/Types';
import {
  NormalizedPaginatedResult,
  PageNumbered,
  PaginatedDomainModelsState,
} from '../../domain-models-paginated/paginatedDomainModels';
import {DomainModelsState} from '../../domain-models/domainModels';

export interface PaginationMetadata {
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface HasComponentId {
  componentId: uuid;
}

export type EntityTypes = keyof (PaginatedDomainModelsState & DomainModelsState);

export type PaginationChangePayload =
  HasComponentId
  & PageNumbered
  & {entityType: EntityTypes};

export type PaginationMetadataPayload =
  NormalizedPaginatedResult
  & {entityType: EntityTypes};

export type OnChangePage = (payload: PaginationChangePayload) => void;

export interface PaginationModel extends PaginationMetadata {
  useCases: {[component: string]: PageNumbered};
}

export type PaginationState = Paginated & Pageable;

type Paginated = {
  [entityType in keyof PaginatedDomainModelsState]: PaginationModel;
};
type Pageable = {
  [entityType in keyof DomainModelsState]?: PaginationModel;
};

// This interface must match Kendo's SortDescriptor (which wants the field as 'string', we add a validating layer)
export interface ApiRequestSortingOptions {
  field: RequestParameter;
  dir?: 'asc' | 'desc';
}

export interface ApiResultSortingOptions {
  direction: 'ASC' | 'DESC';
  property: string;
  ignoreCase: boolean;
  nullHandling: string;
  ascending: boolean;
  descending: boolean;
}

export type Pagination = PageNumbered & PaginationMetadata;

export interface PaginationLookupState<T> extends HasComponentId {
  entityType: keyof T;
  pagination: PaginationState;
}
