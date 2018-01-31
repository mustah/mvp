export interface Pagination {
  first: boolean;
  last: boolean;
  currentPage: number;
  numberOfElements: number;
  size: number;
  sort: SortingOptions[] | null;
  totalElements: number;
  totalPages: number;
}

export interface SelectedPagination  {
  page: number;
  useCase: string;
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
