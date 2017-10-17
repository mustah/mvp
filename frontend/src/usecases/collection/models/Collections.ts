import {NormalizedRows} from '../../common/components/table/table/Table';

export interface CollectionState {
  title: string;
  records: CollectionState[];
  error?: string;
  isFetching: boolean;
  gateways: Gateway;
  categories: Category;
  filter: Filter;
  pagination: Pagination;
}

// TODO manually test that Set is being compiled properly for older browsers
export interface Filter {
  [category: string]: Set<string>;
}

export interface Pagination {
  page: number;
  limit: number;
  total: number;
}

// TODO we must give gateways a type
export type Gateway = NormalizedRows;

// TODO we must give categories a type
export interface Category {
  handled: CollectionCategories;
  unhandled: CollectionCategories;
}

interface CollectionCategories {
  total: number;
  area: {
    count: number;
    entities: Array<{id: string; count: number}>;
  };
  product_model: {
    count: number;
    entities: Array<{id: string; count: number}>;
  };
}
