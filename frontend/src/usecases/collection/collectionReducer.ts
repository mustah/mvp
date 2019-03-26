import {ActionType, getType} from 'typesafe-actions';
import {Period} from '../../components/dates/dateModels';
import {logoutUser} from '../auth/authActions';
import * as actions from './collectionActions';
import {CollectionState} from './collectionModels';

const initialState: CollectionState = {
  isTimePeriodDefault: true,
  isExportingToExcel: false,
  timePeriod: {period: Period.latest},
};

type ActionTypes = ActionType<typeof actions | typeof logoutUser>;

export const collection = (state: CollectionState = initialState, action: ActionTypes): CollectionState => {
  switch (action.type) {
    case getType(actions.setCollectionTimePeriod):
      return {
        ...state,
        timePeriod: action.payload,
        isTimePeriodDefault: false,
      };
    case getType(actions.exportToExcelAction):
      return {...state, isExportingToExcel: true};
    case getType(actions.exportToExcelSuccess):
      return {...state, isExportingToExcel: false};
    case getType(logoutUser):
      return initialState;
    default:
      return state;
  }
};
