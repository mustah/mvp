import {getType} from 'typesafe-actions';
import {Maybe} from '../../../helpers/Maybe';
import {resetReducer} from '../../../reducers/resetReducer';
import {EndPoints} from '../../../services/endPoints';
import {domainModelsGetEntitiesSuccess} from '../../../state/domain-models/domainModelsActions';

import {MeasurementResponse, MeasurementState} from '../../../state/ui/graph/measurement/measurementModels';
import {ActionTypes, initialState} from '../../../state/ui/graph/measurement/measurementReducer';
import {Action, ErrorResponse} from '../../../types/Types';
import {
  removeAllByType,
  selectResolution,
  setReportTimePeriod,
  toggleComparePeriod,
  toggleQuantityById,
  toggleQuantityByType
} from '../../report/reportActions';
import {search} from '../../search/searchActions';
import {setMeterDetailsTimePeriod} from './meterDetailActions';
import {
  meterDetailExportToExcelAction,
  meterDetailExportToExcelSuccess,
  meterDetailMeasurementFailure,
  meterDetailMeasurementRequest,
  meterDetailMeasurementSuccess
} from './meterDetailMeasurementActions';

export const meterDetailMeasurement = (
  state: MeasurementState = initialState,
  action: ActionTypes
): MeasurementState => {
  switch (action.type) {
    case getType(meterDetailMeasurementRequest):
      return {
        ...state,
        isFetching: true,
      };
    case getType(meterDetailMeasurementSuccess):
      return {
        ...state,
        measurementResponse: (action as Action<MeasurementResponse>).payload,
        isFetching: false,
        isSuccessfullyFetched: true,
      };
    case getType(meterDetailMeasurementFailure):
      return {
        ...state,
        error: (action as Action<Maybe<ErrorResponse>>).payload,
        isFetching: false,
        isSuccessfullyFetched: false,
      };
    case getType(meterDetailExportToExcelAction):
      return {...state, isExportingToExcel: true};
    case getType(meterDetailExportToExcelSuccess):
      return {...state, isExportingToExcel: false};
    case domainModelsGetEntitiesSuccess(EndPoints.meterDetails):
    case getType(setMeterDetailsTimePeriod):
    case getType(selectResolution):
    case getType(toggleComparePeriod):
    case getType(setReportTimePeriod):
    case getType(removeAllByType):
    case getType(toggleQuantityByType):
    case getType(toggleQuantityById):
    case getType(search):
      return initialState;
    default:
      return resetReducer(state, action, initialState);
  }
};
