import {normalize} from 'normalizr';
import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {Dispatch} from 'redux';
import {restClient} from '../../services/restClient';
import {SelectionOptions, SelectionParameter} from './models/selectionModels';
import {SelectionState} from './selectionReducer';
import {selectionSchema} from './selectionSchemas';
import {RootState} from '../../reducers/index';

export const SELECTION_REQUEST = 'SELECTION_REQUEST';
export const SELECTION_SUCCESS = 'SELECTION_SUCCESS';
export const SELECTION_FAILURE = 'SELECTION_FAILURE';

export const CLOSE_SELECTION_PAGE = 'CLOSE_SELECTION_PAGE';
export const SET_SELECTION = 'SET_SELECTION';
export const DESELECT_SELECTION = 'DESELECT_SELECTION';

const closeSelectionPage = createEmptyAction(CLOSE_SELECTION_PAGE);

const selectionRequest = createEmptyAction(SELECTION_REQUEST);
const selectionSuccess = createPayloadAction<string, SelectionOptions>(SELECTION_SUCCESS);
const selectionFailure = createPayloadAction<string, SelectionParameter>(SELECTION_FAILURE);

export const setSelection = createPayloadAction<string, SelectionParameter>(SET_SELECTION);
export const deselectSelection = createPayloadAction<string, SelectionParameter>(DESELECT_SELECTION);

export const closeSearch = () => dispatch => {
  dispatch(closeSelectionPage());
  dispatch(routerActions.goBack());
};

export const toggleSelection = (parameter: SelectionParameter) =>
  (dispatch: Dispatch<SelectionState>, getState: () => RootState) => {

    const {entity, id} = parameter;
    const selected = getState().selection.selected[entity];

    // TODO: Perhaps consider getting a boolean for selected unselected from caller.
    selected.includes(id)
      ? dispatch(deselectSelection(parameter))
      : dispatch(setSelection(parameter));
  };

export const fetchSelections = () =>
  async (dispatch: Dispatch<any>) => {
    try {
      dispatch(selectionRequest());
      const {data: selections} = await restClient.get('/selections');
      dispatch(selectionSuccess(normalize(selections, selectionSchema)));
    } catch (error) {
      const {response: {data}} = error;
      dispatch(selectionFailure(data));
    }
  };
