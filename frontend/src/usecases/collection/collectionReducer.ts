import {ActionType, getType} from 'typesafe-actions';
import {Period} from '../../components/dates/dateModels';
import {Maybe} from '../../helpers/Maybe';
import * as reportActions from '../../state/report/reportActions';
import {search} from '../../state/search/searchActions';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Action, ModelSectors, ErrorResponse} from '../../types/Types';
import {logoutUser} from '../auth/authActions';
import * as actions from './collectionActions';
import {CollectionState} from './collectionModels';

const initialState: CollectionState = {
  isTimePeriodDefault: true,
  isExportingToExcel: false,
  timePeriod: {period: Period.latest},
};

type ActionTypes = ActionType<typeof actions | typeof reportActions | typeof search>
  | Action<Maybe<ErrorResponse> | SelectionInterval>;

export const collectionReducerFor =
  (sector: ModelSectors) =>
    (state: CollectionState = initialState, action: ActionTypes): CollectionState => {
  switch (action.type) {
    case getType(actions.setCollectionTimePeriod(sector)):
      return {
        ...state,
        timePeriod: (action.payload as SelectionInterval),
        isTimePeriodDefault: false,
      };
    case getType(actions.exportToExcelAction(sector)):
      return {...state, isExportingToExcel: true};
    case getType(actions.exportToExcelSuccess(sector)):
      return {...state, isExportingToExcel: false};
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};

export const collection = collectionReducerFor(ModelSectors.collection);

export const meterCollection = collectionReducerFor(ModelSectors.meterCollection);
