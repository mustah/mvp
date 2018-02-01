import {HasComponentId} from '../../domain-models/paginatedDomainModels';

export interface Pagination {
  first: boolean;
  last: boolean;
  requestedPage: number;
  currentPage: number;
  numberOfElements: number;
  size: number;
  sort: SortingOptions[] | null;
  totalElements: number;
  totalPages: number;
}

export interface PaginationMetadataPayload extends HasComponentId {
  page: Pagination;
}

export interface PaginationChangePayload extends HasComponentId {
  page: number;
}

export type OnChangePage = (page: number) => void;

export interface PaginationState {
  [useCase: string]: Pagination;
}

export interface SortingOptions {
  direction: 'ASC' | 'DESC';
  property: string;
  ignoreCase: boolean;
  nullHandling: string;
  ascending: boolean;
  descending: boolean;
}
