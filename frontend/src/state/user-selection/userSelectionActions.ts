import {shallowEqual} from 'recompose';
import {Dispatch} from 'redux';
import {createAction, createStandardAction} from 'typesafe-actions';
import {history, routes} from '../../app/routes';
import {DateRange, Period} from '../../components/dates/dateModels';
import {getId} from '../../helpers/collections';
import {Maybe} from '../../helpers/Maybe';
import {GetState, RootState} from '../../reducers/rootReducer';
import {migrateUserSelection, oldParameterNames} from '../../reducers/stateMigrations';
import {isMeterPage} from '../../selectors/routerSelectors';
import {EndPoints} from '../../services/endPoints';
import {firstUpperTranslated} from '../../services/translationService';
import {ErrorResponse, uuid} from '../../types/Types';
import {NormalizedState} from '../domain-models/domainModels';
import {clearError, deleteRequest, fetchIfNeeded, postRequest, putRequest} from '../domain-models/domainModelsActions';
import {showFailMessage} from '../ui/message/messageActions';
import {
  initialSelectionId,
  isValidThreshold,
  OldSelectionParameters,
  ParameterName,
  SelectionItem,
  SelectionParameter,
  ThresholdQuery,
  UserSelection
} from './userSelectionModels';
import {userSelectionsDataFormatter} from './userSelectionSchema';
import {getThreshold, getUserSelection} from './userSelectionSelectors';

export const selectPeriod = createStandardAction('SELECT_PERIOD')<Period>();

export const RESET_SELECTION = 'RESET_SELECTION';
export const SELECT_SAVED_SELECTION = 'SELECT_SAVED_SELECTION';

export const addParameterToSelection = createStandardAction('ADD_PARAMETER_TO_SELECTION')<SelectionParameter>();
export const deselectSelection = createStandardAction('DESELECT_SELECTION')<SelectionParameter>();

export const resetSelection = createAction(RESET_SELECTION);
export const selectSavedSelectionAction = createStandardAction('SELECT_SAVED_SELECTION')<UserSelection>();

export const setThresholdAction = createStandardAction('SET_THRESHOLD')<ThresholdQuery>();

export const setThreshold =
  (threshold: ThresholdQuery | undefined) =>
    (dispatch, getState: GetState) => {
      const state: RootState = getState();
      const oldThreshold = getThreshold(state.userSelection);

      const isOldValid = isValidThreshold(oldThreshold);
      const isNewValid = isValidThreshold(threshold);
      if (
        (!isOldValid && isNewValid) ||
        (isOldValid && threshold === undefined) ||
        (isOldValid && threshold !== undefined && !shallowEqual(threshold, oldThreshold!))
      ) {
        dispatch(setThresholdAction(threshold!));
      }
    };

export const clearUserSelectionErrors = clearError(EndPoints.userSelections);

export const fetchUserSelections = fetchIfNeeded<UserSelection>(
  EndPoints.userSelections,
  'userSelections',
  userSelectionsDataFormatter,
);

export const setCustomDateRange = createStandardAction('SET_CUSTOM_DATE_RANGE')<DateRange>();

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
    const currentSelection: UserSelection = getState().userSelection.userSelection;
    const savedSelectionId = selections.result
      .find((id: uuid) => id === selectedId);

    Maybe.maybe<uuid>(savedSelectionId)
      .map((id: uuid) => selections.entities[id])
      .map((userSelection: UserSelection) => tryMigrateSelectionParameters(dispatch, userSelection))
      .filter((userSelection: UserSelection) => currentSelection.id !== userSelection.id)
      .map((userSelection: UserSelection) => dispatch(selectSavedSelectionAction(userSelection)));
  };

export const selectSelection = (selectionId?: uuid) =>
  (dispatch, getState: GetState) => {
    if (!isMeterPage(getState().routing)) {
      history.push(routes.meters);
    }

    if (selectionId === undefined || selectionId === initialSelectionId) {
      dispatch(resetSelection());
    } else {
      dispatch(selectSavedSelection(selectionId));
    }
  };

const getSelectionItems = (getState: GetState, parameter: ParameterName): SelectionItem[] =>
  getUserSelection(getState().userSelection).selectionParameters[parameter];

export const toggleParameter = (selectionParameter: SelectionParameter) =>
  (dispatch, getState: GetState) => {
    const {parameter, item} = selectionParameter;
    const selectionItems = getSelectionItems(getState, parameter);
    Maybe.maybe<SelectionItem[]>(selectionItems)
      .filter((selected: SelectionItem[]) => selected.map(getId).includes(item.id))
      .map(() => dispatch(deselectSelection(selectionParameter)))
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

export const updateSelection = putRequest<UserSelection, UserSelection>(EndPoints.userSelections, {
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
