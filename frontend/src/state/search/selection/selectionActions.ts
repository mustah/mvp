import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {Period} from '../../../components/dates/dateModels';
import {Maybe} from '../../../helpers/Maybe';
import {GetState} from '../../../reducers/rootReducer';
import {uuid} from '../../../types/Types';
import {paginatedDomainModelsClear} from '../../domain-models-paginated/paginatedDomainModelsActions';
import {domainModelsClear} from '../../domain-models/domainModelsActions';
import {paginationReset} from '../../ui/pagination/paginationActions';
import {FilterParam, SelectionParameter, SelectionState} from './selectionModels';
import {getSelection} from './selectionSelectors';

export const CLOSE_SELECTION_PAGE = 'CLOSE_SELECTION_PAGE';
export const SELECT_PERIOD = 'SELECT_PERIOD';

export const ADD_SELECTION = 'ADD_SELECTION';
export const SET_SELECTION = 'SET_SELECTION';
export const DESELECT_SELECTION = 'DESELECT_SELECTION';
export const SAVE_SELECTION = 'SAVE_SELECTION';
export const UPDATE_SELECTION = 'UPDATE_SELECTION';
export const RESET_SELECTION = 'RESET_SELECTION';
export const SELECT_SAVED_SELECTION = 'SELECT_SAVED_SELECTION';

const closeSelectionPageAction = createEmptyAction(CLOSE_SELECTION_PAGE);
const addSelection = createPayloadAction<string, SelectionParameter>(ADD_SELECTION);
const deselectSelection = createPayloadAction<string, SelectionParameter>(DESELECT_SELECTION);
export const saveSelection = createPayloadAction<string, SelectionState>(SAVE_SELECTION);
const selectSavedSelectionAction = createPayloadAction<string, SelectionState>(SELECT_SAVED_SELECTION);

const updateSelectionAction = createPayloadAction<string, SelectionState>(UPDATE_SELECTION);
const resetSelectionAction = createEmptyAction(RESET_SELECTION);
const setSelectionAction = createPayloadAction<string, SelectionParameter>(SET_SELECTION);
const selectPeriodAction = createPayloadAction<string, Period>(SELECT_PERIOD);

export const closeSelectionPage = () => (dispatch) => {
  dispatch(closeSelectionPageAction());
  dispatch(routerActions.goBack());
};

export const selectSavedSelection = (selectedId: uuid) =>
  (dispatch, getState: GetState) => {
    const savedSelection = getState().searchParameters.saved
      .find((item: SelectionState) => item.id === selectedId);

    Maybe.maybe<SelectionState>(savedSelection)
      .map((selected: SelectionState) => {
        dispatch(selectSavedSelectionAction(selected));
        dispatch(paginationReset());
        dispatch(domainModelsClear());
        dispatch(paginatedDomainModelsClear());
      });
  };

// TODO: ToggleSelection should not be able to accept array values for "id" as the typing suggest now.
export const toggleSelection = (selectionParameter: SelectionParameter) =>
  (dispatch, getState: GetState) => {
    const {parameter, id} = selectionParameter;
    const selected = getSelection(getState().searchParameters).selected[parameter];

    Maybe.maybe<Period | FilterParam[]>(selected)
      .filter((value: Period | FilterParam[]) => Array.isArray(value) && value.includes(id as FilterParam))
      .map(() => dispatch(deselectSelection(selectionParameter)))
      .orElseGet(() => dispatch(addSelection(selectionParameter)));
    dispatch(paginationReset());
    dispatch(domainModelsClear());
    dispatch(paginatedDomainModelsClear());
  };

export const updateSelection = (payload: SelectionState) => (dispatch) => {
  dispatch(updateSelectionAction(payload));
  dispatch(paginationReset());
  dispatch(domainModelsClear());
  dispatch(paginatedDomainModelsClear());
};
export const resetSelection = () => (dispatch) => {
  dispatch(resetSelectionAction());
  dispatch(paginationReset());
  dispatch(domainModelsClear());
  dispatch(paginatedDomainModelsClear());
};
export const setSelection = (payload: SelectionParameter) => (dispatch) => {
  dispatch(setSelectionAction(payload));
  dispatch(paginationReset());
  dispatch(domainModelsClear());
  dispatch(paginatedDomainModelsClear());
};
export const selectPeriod = (payload: Period) => (dispatch) => {
  dispatch(selectPeriodAction(payload));
  dispatch(paginationReset());
  dispatch(domainModelsClear());
  dispatch(paginatedDomainModelsClear());
};
