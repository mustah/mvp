import {ActionType, getType} from 'typesafe-actions';
import {resetReducer} from '../../../reducers/resetReducer';
import {EndPoints} from '../../../services/endPoints';
import {domainModelsGetEntitiesSuccess} from '../../../state/domain-models/domainModelsActions';
import {MeasurementState} from '../../../state/ui/graph/measurement/measurementModels';
import {initialState} from '../../../state/ui/graph/measurement/measurementReducer';
import * as reportActions from '../../report/reportActions';
import {search} from '../../../state/search/searchActions';
import {setMeterDetailsTimePeriod} from './meterDetailActions';
import * as actions from './meterDetailMeasurementActions';

type ActionTypes = ActionType<typeof actions | typeof reportActions | typeof search | typeof setMeterDetailsTimePeriod>;

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
    case domainModelsGetEntitiesSuccess(EndPoints.meterDetails):
    case getType(setMeterDetailsTimePeriod):
      return initialState;
    default:
      return resetReducer(state, action, initialState);
  }
};
