import {uuid} from '../../../types/Types';

export interface CollectionState {
  title: string;

  error?: string;
  isFetching: boolean;
  gateways: Gateways;
  categories: Category;
  filter: Filter;
  pagination: Pagination;
}

// TODO manually test that Set is being compiled properly for older browsers
export interface Filter {
  [category: string]: Set<uuid>;
}

export interface Pagination {
  page: number;
  limit: number;
  total: number;
}

interface Gateway {
  [key: string]: {
    id: string;
    facility: string;
    address: string;
    city: string;
    product_model: string;
    telephone_no: string;
    ip: string;
    port: string;
    status: string;
    position: string;
  };
}

export interface Gateways {
  result: string[];
  entities: {
    gateways: Gateway;
  };
}

// TODO we must give categories a type
export interface Category {
  handled: CollectionCategories;
  unhandled: CollectionCategories;
}

interface CollectionCategories {
  total: number;
  city: {
    count: number;
    entities: Array<{id: string; count: number}>;
  };
  product_model: {
    count: number;
    entities: Array<{id: string; count: number}>;
  };
}
