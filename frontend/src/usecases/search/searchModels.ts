import {SearchState} from './searchReducer';

export type OnSearch = (query?: string) => void;

export interface Query {
  query?: string;
}

export type QueryParameter = Partial<{
  [key in keyof SearchState]: Query;
}>;

export const gatewayQuery = (query?: string): QueryParameter => ({collection: {query}});
export const meterQuery = (query?: string): QueryParameter => ({validation: {query}});
