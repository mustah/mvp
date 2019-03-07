import {getType} from 'typesafe-actions';
import {Maybe} from '../../../helpers/Maybe';
import {resetReducer} from '../../../reducers/resetReducer';
import {EndPoints} from '../../../services/endPoints';
import {domainModelsGetEntitiesSuccess, } from '../../../state/domain-models/domainModelsActions';
import {
  METER_DETAIL_EXPORT_TO_EXCEL_SUCCESS,
  METER_DETAIL_MEASUREMENT_CLEAR_ERROR,
  METER_DETAIL_MEASUREMENT_FAILURE,
  METER_DETAIL_MEASUREMENT_SUCCESS,
  meterDetailExportToExcelAction,
  meterDetailMeasurementRequest,
} from '../../../state/ui/graph/measurement/measurementActions';
import {MeasurementResponse, MeasurementState} from '../../../state/ui/graph/measurement/measurementModels';
import {ActionTypes, initialState} from '../../../state/ui/graph/measurement/measurementReducer';
import {Action, ErrorResponse} from '../../../types/Types';
import {
  addLegendItems,
  removeAllByType,
  selectResolution,
  setReportTimePeriod,
  toggleComparePeriod,
  toggleQuantityById,
  toggleQuantityByType
} from '../../report/reportActions';
import {SEARCH} from '../../search/searchActions';
import {setMeterDetailsTimePeriod} from './meterDetailActions';

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
    case METER_DETAIL_MEASUREMENT_SUCCESS:
      return {
        ...state,
        measurementResponse: (action as Action<MeasurementResponse>).payload,
        isFetching: false,
        isSuccessfullyFetched: true,
      };
    case METER_DETAIL_MEASUREMENT_FAILURE:
      return {
        ...state,
        error: (action as Action<Maybe<ErrorResponse>>).payload,
        isFetching: false,
        isSuccessfullyFetched: false,
      };
    case getType(meterDetailExportToExcelAction):
      return {
        ...state,
        isExportingToExcel: true,
      };
    case METER_DETAIL_EXPORT_TO_EXCEL_SUCCESS:
      return {
        ...state,
        isExportingToExcel: false,
      };
    case METER_DETAIL_MEASUREMENT_CLEAR_ERROR:
    case domainModelsGetEntitiesSuccess(EndPoints.meterDetails):
    case getType(setMeterDetailsTimePeriod):
    case getType(selectResolution):
    case getType(toggleComparePeriod):
    case getType(setReportTimePeriod):
    case getType(addLegendItems):
    case getType(removeAllByType):
    case getType(toggleQuantityByType):
    case getType(toggleQuantityById):
    case SEARCH:
      return initialState;
    default:
      return resetReducer(state, action, initialState);
  }
};
