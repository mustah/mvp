import {throttle} from 'lodash';
import {Dispatch} from 'react-redux';
import {RootState} from '../../reducers/rootReducer';
import {OnPayloadAction, payloadActionOf} from '../../types/Types';
import {collectionQuery, OnSearch, QueryParameter, selectionTreeQuery, validationQuery} from './searchModels';

export const SEARCH = 'SEARCH';
export const SEARCH_SELECTION_TREE = 'SEARCH_SELECTION_TREE';

export const search = payloadActionOf<QueryParameter>(SEARCH);
export const searchSelectionTree = payloadActionOf<QueryParameter>(SEARCH_SELECTION_TREE);

const throttledSearch = throttle(
  (dispatch, parameter: QueryParameter, searchFunction: OnPayloadAction<QueryParameter>) =>
    dispatch(searchFunction(parameter)), 600, {leading: false, trailing: true},
);

const wildcardSearch = (parameter: QueryParameter, searchFunction: OnPayloadAction<QueryParameter>) =>
  (dispatch: Dispatch<RootState>): OnSearch => throttledSearch(dispatch, parameter, searchFunction);

export const collectionSearch = (query?: string) => wildcardSearch(collectionQuery(query), search);
export const validationSearch = (query?: string) => wildcardSearch(validationQuery(query), search);
export const selectionTreeSearch = (query?: string) => wildcardSearch(selectionTreeQuery(query), searchSelectionTree);
