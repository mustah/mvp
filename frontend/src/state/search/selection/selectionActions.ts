import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {Period} from '../../../components/dates/dateModels';
import {Maybe} from '../../../helpers/Maybe';
import {GetState} from '../../../reducers/rootReducer';
import {uuid} from '../../../types/Types';
import {FilterParam, SelectionParameter, SelectionState} from './selectionModels';
import {getSelection} from './selectionSelectors';

export const SELECT_PERIOD = 'SELECT_PERIOD';

export const ADD_PARAMETER_TO_SELECTION = 'ADD_PARAMETER_TO_SELECTION';
export const SET_SELECTION = 'SET_SELECTION';
export const DESELECT_SELECTION = 'DESELECT_SELECTION';
export const SAVE_SELECTION = 'SAVE_SELECTION';
export const SET_CURRENT_SELECTION = 'SET_CURRENT_SELECTION';
export const RESET_SELECTION = 'RESET_SELECTION';
export const SELECT_SAVED_SELECTION = 'SELECT_SAVED_SELECTION';

const addParameterToSelection = createPayloadAction<string, SelectionParameter>(ADD_PARAMETER_TO_SELECTION);
const deselectParameterInSelection = createPayloadAction<string, SelectionParameter>(DESELECT_SELECTION);
export const resetSelection = createEmptyAction(RESET_SELECTION);
export const saveSelection = createPayloadAction<string, SelectionState>(SAVE_SELECTION);
export const selectPeriod = createPayloadAction<string, Period>(SELECT_PERIOD);
export const setCurrentSelection = createPayloadAction<string, SelectionState>(SET_CURRENT_SELECTION);

const selectSavedSelectionAction = createPayloadAction<string, SelectionState>(SELECT_SAVED_SELECTION);

export const closeSelectionPage = () => (dispatch) => {
  dispatch(routerActions.goBack());
};

export const selectSavedSelection = (selectedId: uuid) =>
  (dispatch, getState: GetState) => {
    const savedSelection = getState().searchParameters.saved
      .find((item: SelectionState) => item.id === selectedId);

    Maybe.maybe<SelectionState>(savedSelection)
      .map((selected: SelectionState) => {
        dispatch(selectSavedSelectionAction(selected));
      });
  };

// TODO: toggleSelectionParameter should not be able to accept array values for "id" as the typing suggest now.
export const toggleParameterInSelection = (selectionParameter: SelectionParameter) =>
  (dispatch, getState: GetState) => {
    const {parameter, id} = selectionParameter;
    const selected = getSelection(getState().searchParameters).selected[parameter];

    Maybe.maybe<Period | FilterParam[]>(selected)
      .filter((value: Period | FilterParam[]) => Array.isArray(value) && value.includes(id as FilterParam))
      .map(() => dispatch(deselectParameterInSelection(selectionParameter)))
      .orElseGet(() => dispatch(addParameterToSelection(selectionParameter)));
  };
