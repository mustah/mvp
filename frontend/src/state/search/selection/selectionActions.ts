import {normalize} from 'normalizr';
import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {Dispatch} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {restClient} from '../../../services/restClient';
import {ErrorResponse} from '../../../types/Types';
import {fetchMeters} from '../../domain-models/meter/meterActions';
import {SelectionNormalized, SelectionParameter} from './selectionModels';
import {SelectionState} from './selectionReducer';
import {selectionSchema} from './selectionSchemas';
import {getEncodedUriParameters, getSelection} from './selectionSelectors';

export const SELECTION_REQUEST = 'SELECTION_REQUEST';
export const SELECTION_SUCCESS = 'SELECTION_SUCCESS';
export const SELECTION_FAILURE = 'SELECTION_FAILURE';

export const CLOSE_SELECTION_PAGE = 'CLOSE_SELECTION_PAGE';
export const SET_SELECTION = 'SET_SELECTION';
export const DESELECT_SELECTION = 'DESELECT_SELECTION';

export const closeSelectionPage = createEmptyAction(CLOSE_SELECTION_PAGE);

export const selectionRequest = createEmptyAction(SELECTION_REQUEST);
export const selectionSuccess = createPayloadAction<string, SelectionNormalized>(SELECTION_SUCCESS);
export const selectionFailure = createPayloadAction<string, ErrorResponse>(SELECTION_FAILURE);

export const setSelection = createPayloadAction<string, SelectionParameter>(SET_SELECTION);
export const deselectSelection = createPayloadAction<string, SelectionParameter>(DESELECT_SELECTION);

export const closeSearch = () => dispatch => {
  dispatch(closeSelectionPage());
  dispatch(routerActions.goBack());
};

export const toggleSelection = (selectionParameter: SelectionParameter) =>
  async (dispatch: Dispatch<SelectionState>, getState: () => RootState) => {
    const selectionState: SelectionState = getSelection(getState().searchParameters);
    const {attribute, id} = selectionParameter;

    if (selectionState.selected[attribute].includes(id)) {
      dispatch(deselectSelection(selectionParameter));
    } else {
      dispatch(setSelection(selectionParameter));
    }
    dispatch(fetchMeters(getEncodedUriParameters(getState().searchParameters)));
  };

export const fetchSelections = () =>
  async (dispatch: Dispatch<SelectionState>) => {
    try {
      dispatch(selectionRequest());
      const {data: selections} = await restClient.get('/selections');
      dispatch(selectionSuccess(normalize(selections, selectionSchema)));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(selectionFailure(data));
    }
  };
