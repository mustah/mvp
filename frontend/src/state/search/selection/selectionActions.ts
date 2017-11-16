import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {RootState} from '../../../reducers/rootReducer';
import {Period, uuid} from '../../../types/Types';
import {SelectionParameter, SelectionState} from './selectionModels';
import {getEncodedUriParameters, getSelection} from './selectionSelectors';
import {fetchGateways, fetchMeters} from '../../domain-models/domainModelsActions';

export const CLOSE_SELECTION_PAGE = 'CLOSE_SELECTION_PAGE';

export const SET_SELECTION = 'SET_SELECTION';
export const DESELECT_SELECTION = 'DESELECT_SELECTION';
export const RESET_SELECTION = 'RESET_SELECTION';
export const SELECT_PERIOD = 'SELECT_PERIOD';

export const SAVE_SELECTION = 'SAVE_SELECTION';
export const UPDATE_SELECTION = 'UPDATE_SELECTION';
export const SELECT_SAVED_SELECTION = 'SELECT_SAVED_SELECTION';

export const closeSelectionPageAction = createEmptyAction(CLOSE_SELECTION_PAGE);

export const setSelection = createPayloadAction<string, SelectionParameter>(SET_SELECTION);
export const deselectSelection = createPayloadAction<string, SelectionParameter>(DESELECT_SELECTION);
export const resetSelectionAction = createEmptyAction(RESET_SELECTION);
export const selectPeriodAction = createPayloadAction<string, Period>(SELECT_PERIOD);

export const saveSelectionAction = createPayloadAction<string, SelectionState>(SAVE_SELECTION);
export const updateSelectionAction = createPayloadAction<string, SelectionState>(UPDATE_SELECTION);
export const selectSavedSelectionAction = createPayloadAction<string, SelectionState>(SELECT_SAVED_SELECTION);

// TODO[!must!] do not fetch both every time (good enough for the demo though)
const fetchMetersAndGateways = () =>
  (dispatch, getState: () => RootState) => {
    const encodedUriParameters = getEncodedUriParameters(getState().searchParameters);
    dispatch(fetchMeters(encodedUriParameters));
    dispatch(fetchGateways(encodedUriParameters));
  };

export const closeSelectionPage = () => dispatch => {
  dispatch(closeSelectionPageAction());
  dispatch(routerActions.goBack());
};

export const saveSelection = (selection: SelectionState) =>
  dispatch => {
    dispatch(saveSelectionAction(selection));
    dispatch(selectSavedSelectionAction(selection));
    dispatch(fetchMetersAndGateways());
  };

export const updateSelection = (selection: SelectionState) =>
  dispatch => dispatch(updateSelectionAction(selection));

export const selectSavedSelection = (selectedId: uuid) =>
  (dispatch, getState: () => RootState) => {

    const selected: SelectionState | undefined = getState().searchParameters.saved
      .find((item: SelectionState) => item.id === selectedId);

    if (selected) {
      dispatch(selectSavedSelectionAction(selected));
      dispatch(fetchMetersAndGateways());
    }
  };

export const resetSelection = () =>
  dispatch => {
    dispatch(resetSelectionAction());
    dispatch(fetchMetersAndGateways());
  };

export const toggleSelection = (selectionParameter: SelectionParameter) =>
  (dispatch, getState: () => RootState) => {
    const selectionState: SelectionState = getSelection(getState().searchParameters);
    const {parameter, id} = selectionParameter;

    if (selectionState.selected[parameter].includes(id)) {
      dispatch(deselectSelection(selectionParameter));
    } else {
      dispatch(setSelection(selectionParameter));
    }
    dispatch(fetchMetersAndGateways());
  };

export const addSelection = (selectionParameter: SelectionParameter) =>
  (dispatch, getState: () => RootState) => {
    dispatch(setSelection(selectionParameter));
    dispatch(fetchMetersAndGateways());
  };

export const selectPeriod = (period: Period) =>
  dispatch => {
    dispatch(selectPeriodAction(period));
    dispatch(fetchMetersAndGateways());
  };
