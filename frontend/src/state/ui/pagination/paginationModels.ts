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

export type PaginationChangePayload =
  HasComponentId
  & HasPageNumber
  & {model: keyof (PaginatedDomainModelsState & DomainModelsState)};

export type PaginationMetadataPayload =
  NormalizedPaginatedResult
  & {model: keyof (PaginatedDomainModelsState & DomainModelsState)};

export type OnChangePage = (payload: PaginationChangePayload) => void;

export interface PaginationModel extends PaginationMetadata {
  useCases: {[component: string]: HasPageNumber};
}

/* TODO: check usages of "keyof PaginatedDomainModelsState" if it instead should be
 "keyof PaginatedDomainModelsState & keyof DomainModelsState". */
export type PaginationState = Paginated & Pageable;

type Paginated = {
  [model in keyof PaginatedDomainModelsState] : PaginationModel;
  };
type Pageable = {
  [model in keyof DomainModelsState]?: PaginationModel;
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
  model: keyof T;
  pagination: PaginationState;
}
