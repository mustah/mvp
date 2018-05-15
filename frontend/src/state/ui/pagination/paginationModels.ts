import {uuid} from '../../../types/Types';
import {
  HasPageNumber,
  NormalizedPaginatedResult,
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
  & HasPageNumber
  & {entityType: EntityTypes};

export type PaginationMetadataPayload =
  NormalizedPaginatedResult
  & {entityType: EntityTypes};

export type OnChangePage = (payload: PaginationChangePayload) => void;

export interface PaginationModel extends PaginationMetadata {
  useCases: {[component: string]: HasPageNumber};
}

export type PaginationState = Paginated & Pageable;

type Paginated = {
  [entityType in keyof PaginatedDomainModelsState]: PaginationModel;
};
type Pageable = {
  [entityType in keyof DomainModelsState]?: PaginationModel;
};

export interface SortingOptions {
  direction: 'ASC' | 'DESC';
  property: string;
  ignoreCase: boolean;
  nullHandling: string;
  ascending: boolean;
  descending: boolean;
}

export type Pagination = HasPageNumber & PaginationMetadata;

export interface PaginationLookupState<T> extends HasComponentId {
  entityType: keyof T;
  pagination: PaginationState;
}
