import {throttle} from 'lodash';
import {Dispatch} from 'react-redux';
import {RootState} from '../../reducers/rootReducer';
import {OnPayloadAction, payloadActionOf} from '../../types/Types';
import {makeMeterQuery, OnSearch, QueryParameter} from './searchModels';

export const SEARCH = 'SEARCH';

export const search = payloadActionOf<QueryParameter>(SEARCH);

const throttledSearch = throttle(
  (dispatch, parameter: QueryParameter, searchFunction: OnPayloadAction<QueryParameter>) =>
    dispatch(searchFunction(parameter)), 0, {leading: true, trailing: true},
);

const onSearch = (parameter: QueryParameter, searchFunction: OnPayloadAction<QueryParameter>) =>
  (dispatch: Dispatch<RootState>): OnSearch => throttledSearch(dispatch, parameter, searchFunction);

const clearSearch = (parameter: QueryParameter, searchFunction: OnPayloadAction<QueryParameter>) =>
  (dispatch: Dispatch<RootState>) => dispatch(searchFunction(parameter));

export const validationSearch = (query?: string) => onSearch(makeMeterQuery(query), search);

export const clearValidationSearch = () => clearSearch(makeMeterQuery(), search);
