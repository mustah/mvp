import {NormalizedRows} from '../../common/components/table/table/Table';

export interface CollectionState {
  title: string;
  records: CollectionState[];
  error?: string;
  isFetching: boolean;
  gateways: Gateway;
  categories: Category;
}

// TODO we must give gateways a type
export type Gateway = NormalizedRows;

// TODO we must give categories a type
export interface Category {
  handled: NormalizedRows;
  unhandled: NormalizedRows;
}
