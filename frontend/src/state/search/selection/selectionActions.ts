import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {Dispatch} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {Period, uuid} from '../../../types/Types';
import {fetchMeters} from '../../domain-models/meter/meterActions';
import {SelectionParameter, SelectionState} from './selectionModels';
import {getEncodedUriParameters, getSelection} from './selectionSelectors';

export const CLOSE_SELECTION_PAGE = 'CLOSE_SELECTION_PAGE';
export const SET_SELECTION = 'SET_SELECTION';
export const DESELECT_SELECTION = 'DESELECT_SELECTION';
export const SELECT_PERIOD = 'SELECT_PERIOD';
export const SELECT_SAVED_SELECTION = 'SELECT_SAVED_SELECTION';

export const closeSelectionPage = createEmptyAction(CLOSE_SELECTION_PAGE);

export const setSelection = createPayloadAction<string, SelectionParameter>(SET_SELECTION);
export const deselectSelection = createPayloadAction<string, SelectionParameter>(DESELECT_SELECTION);

export const selectPeriodAction = createPayloadAction<string, Period>(SELECT_PERIOD);
export const selectSavedSelectionAction = createPayloadAction<string, uuid>(SELECT_SAVED_SELECTION);

export const closeSearch = () => dispatch => {
  dispatch(closeSelectionPage());
  dispatch(routerActions.goBack());
};

export const selectPeriod = (period: Period) =>
  async (dispatch, getState: () => RootState) => {
    dispatch(selectPeriodAction(period));
    dispatch(fetchMeters(getEncodedUriParameters(getState().searchParameters)));
  };

export const selectSavedSelection = (selectedId: uuid) =>
  (dispatch) => {
    dispatch(selectSavedSelectionAction(selectedId));
  };

export const toggleSelection = (selectionParameter: SelectionParameter) =>
  async (dispatch: Dispatch<SelectionState>, getState: () => RootState) => {
    const selectionState: SelectionState = getSelection(getState().searchParameters);
    const {parameter, id} = selectionParameter;

    if (selectionState.selected[parameter].includes(id)) {
      dispatch(deselectSelection(selectionParameter));
    } else {
      dispatch(setSelection(selectionParameter));
    }
    dispatch(fetchMeters(getEncodedUriParameters(getState().searchParameters)));
  };
