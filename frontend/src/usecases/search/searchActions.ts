import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {Dispatch} from 'redux';
import {SearchParameter} from './models/searchParameterModels';
import {SearchParametersState} from './searchReducer';

export const CLOSE_SEARCH = 'CLOSE_SEARCH';
export const SELECT_SEARCH_OPTION = 'SELECT_SEARCH_OPTION';

const closeSearchAction = createEmptyAction(CLOSE_SEARCH);
const selectSearchOptionAction = createPayloadAction<string, SearchParameter>(SELECT_SEARCH_OPTION);

export const closeSearch = () => dispatch => {
  dispatch(closeSearchAction());
  dispatch(routerActions.goBack());
};

export const selectSearchOption = (searchParameter: SearchParameter) =>
  (dispatch: Dispatch<SearchParametersState>) => {
    dispatch(selectSearchOptionAction(searchParameter));
  };
