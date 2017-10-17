import {normalize} from 'normalizr';
import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {Dispatch} from 'redux';
import {restClient} from '../../services/restClient';
import {SearchOptions, SearchParameter} from './models/searchModels';
import {SearchState} from './searchReducer';
import {searchOptionsSchema} from './searchSchemas';

export const SEARCH_OPTIONS_REQUEST = 'SEARCH_OPTIONS_REQUEST';
export const SEARCH_OPTIONS_SUCCESS = 'SEARCH_OPTIONS_SUCCESS';
export const SEARCH_OPTIONS_FAILURE = 'SEARCH_OPTIONS_FAILURE';

export const CLOSE_SEARCH = 'CLOSE_SEARCH';
export const SELECT_SEARCH_OPTION = 'SELECT_SEARCH_OPTION';

const closeSearchAction = createEmptyAction(CLOSE_SEARCH);

const searchOptionsRequest = createEmptyAction(SEARCH_OPTIONS_REQUEST);
const searchOptionsSuccess = createPayloadAction<string, SearchOptions>(SEARCH_OPTIONS_SUCCESS);
const searchOptionsFailure = createPayloadAction<string, SearchParameter>(SEARCH_OPTIONS_FAILURE);

export const selectSearchOptionAction = createPayloadAction<string, SearchParameter>(SELECT_SEARCH_OPTION);

export const closeSearch = () => dispatch => {
  dispatch(closeSearchAction());
  dispatch(routerActions.goBack());
};

export const selectSearchOption = (searchParameter: SearchParameter) =>
  (dispatch: Dispatch<SearchState>) => {
    dispatch(selectSearchOptionAction(searchParameter));
  };

export const fetchSearchOptions = () =>
  async (dispatch: Dispatch<any>) => {
    try {
      dispatch(searchOptionsRequest());
      const {data: searchOptions} = await restClient.get('/search-options');
      dispatch(searchOptionsSuccess(normalize(searchOptions, searchOptionsSchema)));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(searchOptionsFailure(data));
    }
  };
