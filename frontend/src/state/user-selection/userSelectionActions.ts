import {routerActions} from 'react-router-redux';
import {shallowEqual} from 'recompose';
import {Dispatch} from 'redux';
import {DateRange, Period} from '../../components/dates/dateModels';
import {getId} from '../../helpers/collections';
import {Maybe} from '../../helpers/Maybe';
import {GetState, RootState} from '../../reducers/rootReducer';
import {migrateUserSelection, oldParameterNames} from '../../reducers/stateMigrations';
import {EndPoints} from '../../services/endPoints';
import {firstUpperTranslated} from '../../services/translationService';
import {emptyActionOf, ErrorResponse, payloadActionOf, uuid} from '../../types/Types';
import {NormalizedState} from '../domain-models/domainModels';
import {clearError, deleteRequest, fetchIfNeeded, postRequest, putRequest} from '../domain-models/domainModelsActions';
import {showFailMessage} from '../ui/message/messageActions';
import {
  OldSelectionParameters,
  ParameterName,
  SelectionItem,
  SelectionParameter,
  ThresholdQuery,
  UserSelection
} from './userSelectionModels';
import {userSelectionsDataFormatter} from './userSelectionSchema';
import {getUserSelection} from './userSelectionSelectors';

export const SELECT_PERIOD = 'SELECT_PERIOD';
export const SET_CUSTOM_DATE_RANGE = 'SET_CUSTOM_DATE_RANGE';

export const selectPeriod = payloadActionOf<Period>(SELECT_PERIOD);

export const ADD_PARAMETER_TO_SELECTION = 'ADD_PARAMETER_TO_SELECTION';
export const DESELECT_SELECTION = 'DESELECT_SELECTION';
export const RESET_SELECTION = 'RESET_SELECTION';
export const SELECT_SAVED_SELECTION = 'SELECT_SAVED_SELECTION';

export const addParameterToSelection = payloadActionOf<SelectionParameter>(ADD_PARAMETER_TO_SELECTION);
const deselectParameterInSelection = payloadActionOf<SelectionParameter>(DESELECT_SELECTION);
export const resetSelection = emptyActionOf(RESET_SELECTION);
const selectSavedSelectionAction = payloadActionOf<UserSelection>(SELECT_SAVED_SELECTION);

export const SET_THRESHOLD = 'SET_THRESHOLD';
const actuallySetThreshold = payloadActionOf<ThresholdQuery>(SET_THRESHOLD);
export const setThreshold =
  (threshold: ThresholdQuery | undefined) =>
    (dispatch, getState: GetState) => {
      const newThresholdIsComplete: boolean =
        threshold !== undefined &&
        Object.keys(threshold).every(key => (threshold[key] as string).length > 0);

      const oldThreshold: ThresholdQuery | undefined =
        getState().userSelection.userSelection.selectionParameters.threshold;

      if (
        (oldThreshold === undefined && newThresholdIsComplete) ||
        (oldThreshold !== undefined && threshold === undefined) ||
        (oldThreshold !== undefined && threshold !== undefined && !shallowEqual(threshold, oldThreshold))
      ) {
        dispatch(actuallySetThreshold(threshold as ThresholdQuery));
      }
    };

export const clearUserSelectionErrors = clearError(EndPoints.userSelections);

export const closeSelectionPage = () => (dispatch) => dispatch(routerActions.goBack());

export const fetchUserSelections = fetchIfNeeded<UserSelection>(
  EndPoints.userSelections,
  'userSelections',
  userSelectionsDataFormatter,
);

export const setCustomDateRange = payloadActionOf<DateRange>(SET_CUSTOM_DATE_RANGE);

const oldParameterKeys: Array<keyof OldSelectionParameters> =
  [...oldParameterNames, 'addresses', 'cities'];

const hasNoObjectsAsValues = (values: Array<keyof OldSelectionParameters>) =>
  Array.from(values).some((value) => typeof value !== 'object');

export const shouldMigrateSelectionParameters =
  (selectionParameters = {}): boolean =>
    Object.keys(selectionParameters)
      .filter((parameter: keyof OldSelectionParameters) => oldParameterKeys.includes(parameter))
      .map((parameter: keyof OldSelectionParameters) => selectionParameters[parameter])
      .filter(hasNoObjectsAsValues)
      .length > 0;

const tryMigrateSelectionParameters =
  (dispatch, userSelection: UserSelection): UserSelection => {
    if (shouldMigrateSelectionParameters(userSelection.selectionParameters)) {
      const newSelection = migrateUserSelection(userSelection);
      dispatch(updateSelection(newSelection));
      return newSelection;
    } else {
      return userSelection;
    }
  };

export const selectSavedSelection = (selectedId: uuid) =>
  (dispatch, getState: GetState) => {
    const selections: NormalizedState<UserSelection> = getState().domainModels.userSelections;
    const savedSelectionId = selections.result
      .find((id: uuid) => id === selectedId);

    Maybe.maybe<uuid>(savedSelectionId)
      .map((id: uuid) => selections.entities[id])
      .map((userSelection: UserSelection) => tryMigrateSelectionParameters(dispatch, userSelection))
      .map((userSelection: UserSelection) => dispatch(selectSavedSelectionAction(userSelection)));
  };

const getSelectionItems = (getState: GetState, parameter: ParameterName): SelectionItem[] =>
  getUserSelection(getState().userSelection).selectionParameters[parameter];

export const toggleParameter = (selectionParameter: SelectionParameter) =>
  (dispatch, getState: GetState) => {
    const {parameter, item} = selectionParameter;
    const selectionItems = getSelectionItems(getState, parameter);
    Maybe.maybe<SelectionItem[]>(selectionItems)
      .filter((selected: SelectionItem[]) => selected.map(getId).includes(item.id))
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
