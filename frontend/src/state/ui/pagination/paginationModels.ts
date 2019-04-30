import {SortDescriptor} from '@progress/kendo-data-query';
import {RequestParameter} from '../../../helpers/urlFactory';
import {
  NormalizedPaginatedResult,
  PageNumbered,
  PaginatedDomainModelsState,
} from '../../domain-models-paginated/paginatedDomainModels';
import {DomainModelsState} from '../../domain-models/domainModels';

interface PaginationMetaData {
  size: number;
  totalElements: number;
  totalPages: number;
}

export type Pagination = PageNumbered & PaginationMetaData;

export type EntityTypes = keyof (PaginatedDomainModelsState & DomainModelsState);

export interface EntityTyped {
  entityType: EntityTypes;
}

export type ChangePagePayload = PageNumbered & EntityTyped;

export type PaginationMetadataPayload = NormalizedPaginatedResult & {entityType: EntityTypes};

export type OnChangePage = (payload: ChangePagePayload) => void;

export type PaginationState = {
  [entityType in keyof PaginatedDomainModelsState]: PaginationMetaData & PageNumbered;
};

export interface ApiRequestSortingOptions extends SortDescriptor {
  field: RequestParameter;
}

export interface ApiResultSortingOptions {
  direction: 'ASC' | 'DESC';
  property: string;
  ignoreCase: boolean;
  nullHandling: string;
  ascending: boolean;
  descending: boolean;
}

export interface PaginationLookupState {
  entityType: keyof PaginatedDomainModelsState;
  pagination: PaginationState;
}
