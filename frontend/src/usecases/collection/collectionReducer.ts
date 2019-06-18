import {ActionType, getType} from 'typesafe-actions';
import {Period} from '../../components/dates/dateModels';
import {logoutUser} from '../auth/authActions';
import {
  collectionStatsExportToExcel,
  collectionStatsExportToExcelSuccess,
  meterCollectionStatsExportToExcel,
  meterCollectionStatsExportToExcelSuccess,
  setCollectionStatsTimePeriod,
  setMeterCollectionStatsTimePeriod
} from './collectionActions';
import {CollectionState} from './collectionModels';

const initialState: CollectionState = {
  isExportingToExcel: false,
  timePeriod: {period: Period.yesterday},
};

type ActionTypes = ActionType<typeof setCollectionStatsTimePeriod
  | typeof setMeterCollectionStatsTimePeriod
  | typeof collectionStatsExportToExcel
  | typeof meterCollectionStatsExportToExcel
  | typeof collectionStatsExportToExcelSuccess
  | typeof meterCollectionStatsExportToExcelSuccess
  | typeof logoutUser>;

export const collection =
  (state: CollectionState = initialState, action: ActionTypes): CollectionState => {
    switch (action.type) {
      case getType(setCollectionStatsTimePeriod):
        return {...state, timePeriod: action.payload};
      case getType(setMeterCollectionStatsTimePeriod):
        return {...state, timePeriod: action.payload};
      case getType(collectionStatsExportToExcel):
        return {...state, isExportingToExcel: true};
      case getType(meterCollectionStatsExportToExcel):
        return {...state, isExportingToExcel: true};
      case getType(collectionStatsExportToExcelSuccess):
        return {...state, isExportingToExcel: false};
      case getType(meterCollectionStatsExportToExcelSuccess):
        return {...state, isExportingToExcel: false};
      case getType(logoutUser):
        return initialState;
      default:
        return state;
    }
  };
