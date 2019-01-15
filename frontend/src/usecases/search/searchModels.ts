import {SearchState} from './searchReducer';

export type OnSearch = (query?: string) => void;

export interface Query {
  query?: string;
}

export type QueryParameter = Partial<{
  [key in keyof SearchState]: Query;
}>;

export const collectionQuery = (query?: string): QueryParameter => ({collection: {query}});
export const meterQuery = (query?: string): QueryParameter => ({validation: {query}});
export const selectionTreeQuery = (query?: string): QueryParameter => ({selectionTree: {query}});
