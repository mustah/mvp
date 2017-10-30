export interface Pagination {
  page: number;
  limit: number;
}

export type ChangePage = (page: number) => any;

export interface PaginationProps {
  pagination: Pagination;
  numOfEntities: number;
  changePage: ChangePage;
}

export interface PaginationState {
  dashboard: Pagination;
  collection: Pagination;
  validation: Pagination;
  selection: Pagination;
}
