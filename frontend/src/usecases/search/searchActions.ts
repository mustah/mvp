import {throttle} from 'lodash';
import {Dispatch} from 'react-redux';
import {RootState} from '../../reducers/rootReducer';
import {OnPayloadAction, payloadActionOf} from '../../types/Types';
import {gatewayQuery, OnSearch, QueryParameter, selectionTreeQuery, meterQuery} from './searchModels';

export const SEARCH = 'SEARCH';
export const SEARCH_SELECTION_TREE = 'SEARCH_SELECTION_TREE';

export const search = payloadActionOf<QueryParameter>(SEARCH);
export const searchSelectionTree = payloadActionOf<QueryParameter>(SEARCH_SELECTION_TREE);

const throttledSearch = throttle(
  (dispatch, parameter: QueryParameter, searchFunction: OnPayloadAction<QueryParameter>) =>
    dispatch(searchFunction(parameter)), 600, {leading: false, trailing: true},
);

const onSearch = (parameter: QueryParameter, searchFunction: OnPayloadAction<QueryParameter>) =>
  (dispatch: Dispatch<RootState>): OnSearch => throttledSearch(dispatch, parameter, searchFunction);

const clearSearch = (parameter: QueryParameter, searchFunction: OnPayloadAction<QueryParameter>) =>
  (dispatch: Dispatch<RootState>) => dispatch(searchFunction(parameter));

export const collectionSearch = (query?: string) => onSearch(gatewayQuery(query), search);
export const validationSearch = (query?: string) => onSearch(meterQuery(query), search);
export const selectionTreeSearch = (query?: string) => onSearch(selectionTreeQuery(query), searchSelectionTree);

export const clearCollectionSearch = () => clearSearch(gatewayQuery(), search);
export const clearValidationSearch = () => clearSearch(meterQuery(), search);
export const clearSelectionTreeSearch = () => clearSearch(selectionTreeQuery(), searchSelectionTree);
