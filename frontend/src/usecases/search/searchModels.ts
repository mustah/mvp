export type OnSearch = (query?: string) => void;

export interface Query {
  query?: string;
}

export interface QueryParameter {
  [key: string]: Query;
}

export const collectionQuery = (query?: string): QueryParameter => ({collection: {query}});
export const validationQuery = (query?: string): QueryParameter => ({validation: {query}});
