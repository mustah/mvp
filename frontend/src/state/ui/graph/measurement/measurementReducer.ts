import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {Maybe} from '../../../../helpers/Maybe';
import {Action, ErrorResponse} from '../../../../types/Types';
import {
  addLegendItems,
  removeAllByMedium,
  selectResolution,
  setReportTimePeriod,
  toggleQuantityById,
  toggleQuantityByMedium
} from '../../../../usecases/report/reportActions';
import {SEARCH} from '../../../../usecases/search/searchActions';
import {resetReducer} from '../../../domain-models/domainModelsReducer';
import {
  EXPORT_TO_EXCEL_SUCCESS,
  exportToExcelAction,
  MEASUREMENT_CLEAR_ERROR,
  MEASUREMENT_FAILURE,
  MEASUREMENT_SUCCESS,
  measurementRequest
} from './measurementActions';
import {MeasurementResponse, MeasurementState} from './measurementModels';

export type ActionTypes = | EmptyAction<string> | Action<MeasurementResponse | Maybe<ErrorResponse>>;

export const initialState: MeasurementState = {
  isFetching: false,
  isSuccessfullyFetched: false,
  error: Maybe.nothing(),
  measurementResponse: {
    measurements: [],
    average: [],
  },
  isExportingToExcel: false,
};

export const measurement = (state: MeasurementState = initialState, action: ActionTypes): MeasurementState => {
  switch (action.type) {
    case getType(measurementRequest):
      return {
        ...state,
        isFetching: true,
      };
    case MEASUREMENT_SUCCESS:
      return {
        ...state,
        measurementResponse: (action as Action<MeasurementResponse>).payload,
        isFetching: false,
        isSuccessfullyFetched: true,
      };
    case MEASUREMENT_FAILURE:
      return {
        ...state,
        error: (action as Action<Maybe<ErrorResponse>>).payload,
        isFetching: false,
        isSuccessfullyFetched: false,
      };
    case getType(exportToExcelAction):
      return {
        ...state,
        isExportingToExcel: true,
      };
    case EXPORT_TO_EXCEL_SUCCESS:
      return {
        ...state,
        isExportingToExcel: false,
      };
    case MEASUREMENT_CLEAR_ERROR:
    case getType(selectResolution):
    case getType(setReportTimePeriod):
    case getType(addLegendItems):
    case getType(removeAllByMedium):
    case getType(toggleQuantityByMedium):
    case getType(toggleQuantityById):
    case SEARCH:
      return initialState;
    default:
      return resetReducer(state, action, initialState);
  }
};
