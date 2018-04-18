import {createEmptyAction, createPayloadAction} from 'react-redux-typescript';
import {routerActions} from 'react-router-redux';
import {Dispatch} from 'redux';
import {Period} from '../../../components/dates/dateModels';
import {Maybe} from '../../../helpers/Maybe';
import {GetState, RootState} from '../../../reducers/rootReducer';
import {EndPoints} from '../../../services/endPoints';
import {firstUpperTranslated} from '../../../services/translationService';
import {ErrorResponse, uuid} from '../../../types/Types';
import {NormalizedState} from '../../domain-models/domainModels';
import {deleteRequest, fetchIfNeeded, postRequest, putRequest} from '../../domain-models/domainModelsActions';
import {showFailMessage} from '../../ui/message/messageActions';
import {FilterParam, SelectionParameter, UserSelection} from './selectionModels';
import {userSelectionSchema} from './selectionSchema';
import {getSelection} from './selectionSelectors';

export const SELECT_PERIOD = 'SELECT_PERIOD';

export const ADD_PARAMETER_TO_SELECTION = 'ADD_PARAMETER_TO_SELECTION';
export const SET_SELECTION = 'SET_SELECTION';
export const DESELECT_SELECTION = 'DESELECT_SELECTION';
export const SET_CURRENT_SELECTION = 'SET_CURRENT_SELECTION';
export const RESET_SELECTION = 'RESET_SELECTION';
export const SELECT_SAVED_SELECTION = 'SELECT_SAVED_SELECTION';

const addParameterToSelection = createPayloadAction<string, SelectionParameter>(
  ADD_PARAMETER_TO_SELECTION);
const deselectParameterInSelection = createPayloadAction<string, SelectionParameter>(
  DESELECT_SELECTION);
export const resetSelection = createEmptyAction(RESET_SELECTION);
export const selectPeriod = createPayloadAction<string, Period>(SELECT_PERIOD);

const selectSavedSelectionAction = createPayloadAction<string, UserSelection>(SELECT_SAVED_SELECTION);

export const closeSelectionPage = () => (dispatch) => {
  dispatch(routerActions.goBack());
};

export const fetchUserSelections = fetchIfNeeded<UserSelection>(
  EndPoints.userSelections,
  userSelectionSchema,
  'userSelections',
);

export const selectSavedSelection = (selectedId: uuid) =>
  (dispatch, getState: GetState) => {
    const selections: NormalizedState<UserSelection> = getState().domainModels.userSelections;
    const savedSelectionId = selections.result
      .find((id: uuid) => id === selectedId);

    Maybe.maybe<uuid>(savedSelectionId)
      .map((id: uuid) => {
        dispatch(selectSavedSelectionAction(selections.entities[id]));
      });
  };

// TODO: toggleSelectionParameter should not be able to accept array values for "id" as the typing suggest now.
export const toggleParameterInSelection = (selectionParameter: SelectionParameter) =>
  (dispatch, getState: GetState) => {
    const {parameter, id} = selectionParameter;
    const selected = getSelection(getState().searchParameters).selectionParameters[parameter];

    Maybe.maybe<Period | FilterParam[]>(selected)
      .filter((value: Period | FilterParam[]) => Array.isArray(value) && value.includes(id as FilterParam))
      .map(() => dispatch(deselectParameterInSelection(selectionParameter)))
      .orElseGet(() => dispatch(addParameterToSelection(selectionParameter)));
  };

export const saveSelection = postRequest<UserSelection>(EndPoints.userSelections, {
  afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to create selection: {{error}}',
      {error: error.message},
    )));
  },
});

export const updateSelection = putRequest<UserSelection>(EndPoints.userSelections, {
  afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to update selection: {{error}}',
      {error: error.message},
    )));
  },
});

export const deleteUserSelection = deleteRequest<UserSelection>(EndPoints.userSelections, {
  afterFailure: (error: ErrorResponse, dispatch: Dispatch<RootState>) => {
    dispatch(showFailMessage(firstUpperTranslated(
      'failed to delete selection: {{error}}',
      {error: error.message},
    )));
  },
});
