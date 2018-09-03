import {throttle} from 'lodash';
import {Dispatch} from 'react-redux';
import {RootState} from '../../reducers/rootReducer';
import {payloadActionOf} from '../../types/Types';
import {collectionQuery, OnSearch, QueryParameter, selectionTreeQuery, validationQuery} from './searchModels';

export const SEARCH = 'SEARCH';

export const search = payloadActionOf<QueryParameter>(SEARCH);

const throttledSearch = throttle(
  (dispatch, parameter: QueryParameter) =>
    dispatch(search(parameter)), 600, {leading: false, trailing: true},
);

const wildcardSearch = (parameter: QueryParameter) =>
  (dispatch: Dispatch<RootState>): OnSearch => throttledSearch(dispatch, parameter);

export const collectionSearch = (query?: string) => wildcardSearch(collectionQuery(query));
export const validationSearch = (query?: string) => wildcardSearch(validationQuery(query));
export const selectionTreeSearch = (query?: string) => wildcardSearch(selectionTreeQuery(query));
