export interface CollectionState {
  title: string;
  records: CollectionState[];
  error?: string;
  isFetching: boolean;
  gateways: Gateway[];
  categories: Category[];
}

// TODO we must give gateways a type
export type Gateway = any;

// TODO we must give categories a type
export type Category = any;
