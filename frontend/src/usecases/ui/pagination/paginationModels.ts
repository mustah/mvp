export interface Pagination {
  page: number;
  limit: number;
}

export interface PaginationState {
  dashboard: Pagination;
  collection: Pagination;
  validation: Pagination;
  selection: Pagination;
}
