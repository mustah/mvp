import {ActionType, getType} from 'typesafe-actions';
import {isOnMeterDetailsPage} from '../../../app/routes';
import {resetReducer} from '../../../reducers/resetReducer';
import {locationChange} from '../../../state/location/locationActions';
import {MeasurementState} from '../../../state/ui/graph/measurement/measurementModels';
import {initialState} from '../../../state/ui/graph/measurement/measurementReducer';
import {setMeterDetailsTimePeriod} from './meterDetailActions';
import * as actions from './meterDetailMeasurementActions';

type ActionTypes = ActionType<typeof actions
  | typeof setMeterDetailsTimePeriod
  | typeof locationChange>;

export const meterDetailMeasurement = (
  state: MeasurementState = initialState,
  action: ActionTypes
): MeasurementState => {
  switch (action.type) {
    case getType(actions.meterDetailMeasurementRequest):
      return {
        ...state,
        isFetching: true,
      };
    case getType(actions.meterDetailMeasurementSuccess):
      return {
        ...state,
        measurementResponse: action.payload,
        isFetching: false,
        isSuccessfullyFetched: true,
      };
    case getType(actions.meterDetailMeasurementFailure):
      return {
        ...state,
        error: action.payload,
        isFetching: false,
        isSuccessfullyFetched: false,
      };
    case getType(actions.meterDetailExportToExcelAction):
      return {...state, isExportingToExcel: true};
    case getType(actions.meterDetailExportToExcelSuccess):
      return {...state, isExportingToExcel: false};
    case getType(setMeterDetailsTimePeriod):
      return initialState;
    case getType(locationChange):
      return isOnMeterDetailsPage(action.payload.pathname)
        ? state
        : initialState;
    default:
      return resetReducer(state, action, initialState);
  }
};
