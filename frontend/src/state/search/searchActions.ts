import {throttle} from 'lodash';
import {Dispatch} from 'redux';
import {createStandardAction} from 'typesafe-actions';
import {OnPayloadAction} from '../../types/Types';
import {makeMeterQuery, OnSearch, QueryParameter} from './searchModels';

export const search = createStandardAction('SEARCH')<QueryParameter>();

const throttledSearch = throttle(
  (dispatch, parameter: QueryParameter, searchFunction: OnPayloadAction<QueryParameter>) =>
    dispatch(searchFunction(parameter)), 0, {leading: true, trailing: true},
);

const onSearch = (parameter: QueryParameter, searchFunction: OnPayloadAction<QueryParameter>) =>
  (dispatch: Dispatch): OnSearch => throttledSearch(dispatch, parameter, searchFunction);

const clearSearch = (parameter: QueryParameter, searchFunction: OnPayloadAction<QueryParameter>) =>
  (dispatch: Dispatch) => dispatch(searchFunction(parameter));

export const validationSearch = (query?: string) => onSearch(makeMeterQuery(query), search);

export const clearValidationSearch = () => clearSearch(makeMeterQuery(), search);
