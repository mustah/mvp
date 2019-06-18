import {ActionType, getType} from 'typesafe-actions';
import {isOnMeterDetailsPage} from '../../../app/routes';
import {resetReducer} from '../../../reducers/resetReducer';
import {locationChange} from '../../../state/location/locationActions';
import {MeasurementState} from '../../../state/ui/graph/measurement/measurementModels';
import {initialState} from '../../../state/ui/graph/measurement/measurementReducer';
import * as actions from './meterDetailMeasurementActions';

type ActionTypes = ActionType<typeof actions | typeof locationChange>;

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
    case getType(actions.selectResolution):
    case getType(actions.setTimePeriod):
      return initialState;
    case getType(locationChange):
      return isOnMeterDetailsPage(action.payload.location.pathname) ? initialState : state;
    default:
      return resetReducer(state, action, initialState);
  }
};
