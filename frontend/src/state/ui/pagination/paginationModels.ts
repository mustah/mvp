export interface Pagination {
  page: number;
  limit: number;
}

export interface SelectedPagination  {
  page: number;
  useCase: string;
}

export type OnChangePage = (page: number) => void;

export interface PaginationProps {
  pagination: Pagination;
  numOfEntities: number;
  changePage: OnChangePage;
}

export interface PaginationState {
  dashboard: Pagination;
  collection: Pagination;
  validation: Pagination;
  selection: Pagination;
}
