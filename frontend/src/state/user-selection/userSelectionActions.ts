import {routerActions} from 'react-router-redux';
import {Dispatch} from 'redux';
import {DateRange, Period} from '../../components/dates/dateModels';
import {Maybe} from '../../helpers/Maybe';
import {GetState, RootState} from '../../reducers/rootReducer';
import {EndPoints} from '../../services/endPoints';
import {firstUpperTranslated} from '../../services/translationService';
import {emptyActionOf, ErrorResponse, payloadActionOf, uuid} from '../../types/Types';
import {NormalizedState} from '../domain-models/domainModels';
import {
  deleteRequest,
  fetchIfNeeded,
  postRequest,
  putRequest,
} from '../domain-models/domainModelsActions';
import {showFailMessage} from '../ui/message/messageActions';
import {FilterParam, SelectionParameter, UserSelection} from './userSelectionModels';
import {userSelectionSchema} from './userSelectionSchema';
import {getSelection} from './userSelectionSelectors';

export const SELECT_PERIOD = 'SELECT_PERIOD';

export const ADD_PARAMETER_TO_SELECTION = 'ADD_PARAMETER_TO_SELECTION';
export const SET_SELECTION = 'SET_SELECTION';
export const DESELECT_SELECTION = 'DESELECT_SELECTION';
export const RESET_SELECTION = 'RESET_SELECTION';
export const SELECT_SAVED_SELECTION = 'SELECT_SAVED_SELECTION';
export const SET_CUSTOM_DATE_RANGE = 'SET_CUSTOM_DATE_RANGE';

const addParameterToSelection = payloadActionOf<SelectionParameter>(ADD_PARAMETER_TO_SELECTION);
const deselectParameterInSelection = payloadActionOf<SelectionParameter>(DESELECT_SELECTION);
export const resetSelection = emptyActionOf(RESET_SELECTION);
export const selectPeriod = payloadActionOf<Period>(SELECT_PERIOD);

const selectSavedSelectionAction = payloadActionOf<UserSelection>(SELECT_SAVED_SELECTION);

export const closeSelectionPage = () => (dispatch) => {
  dispatch(routerActions.goBack());
};

export const fetchUserSelections = fetchIfNeeded<UserSelection>(
  EndPoints.userSelections,
  userSelectionSchema,
  'userSelections',
);

export const setCustomDateRange = payloadActionOf<DateRange>(SET_CUSTOM_DATE_RANGE);

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

// TODO: toggleSelectionParameter should not be able to accept array values for "id" as the typing
// suggest now.
export const toggleParameterInSelection = (selectionParameter: SelectionParameter) =>
  (dispatch, getState: GetState) => {
    const {parameter, id} = selectionParameter;
    const selected: UserSelection = getSelection(getState().userSelection);

    if (!(parameter in selected.selectionParameters)) {
      return;
    }
    const selectedSection = selected.selectionParameters[parameter];

    Maybe.maybe<Period | FilterParam[]>(selectedSection)
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
