export interface Pagination {
  page: number;
  limit: number;
}

export interface SelectedPagination  {
  page: number;
  useCase: string;
}

export type OnChangePage = (page: number) => void;

export interface PaginationState {
  collection: Pagination;
  validation: Pagination;
  selection: Pagination;
}
