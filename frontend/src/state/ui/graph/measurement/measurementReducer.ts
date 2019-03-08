import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {Maybe} from '../../../../helpers/Maybe';
import {resetReducer} from '../../../../reducers/resetReducer';
import {Action, ErrorResponse} from '../../../../types/Types';
import {
  addLegendItems,
  removeAllByType,
  selectResolution,
  setReportTimePeriod,
  toggleComparePeriod,
  toggleQuantityById,
  toggleQuantityByType,
  toggleShowAverage
} from '../../../../usecases/report/reportActions';
import {search} from '../../../../usecases/search/searchActions';
import {
  exportToExcelAction,
  exportToExcelSuccess,
  measurementClearError,
  measurementFailure,
  measurementRequest,
  measurementSuccess
} from './measurementActions';
import {MeasurementResponse, MeasurementState} from './measurementModels';

export type ActionTypes = | EmptyAction<string> | Action<MeasurementResponse | Maybe<ErrorResponse>>;

export const initialState: MeasurementState = {
  isFetching: false,
  isSuccessfullyFetched: false,
  error: Maybe.nothing(),
  measurementResponse: {
    average: [],
    compare: [],
    measurements: [],
  },
  isExportingToExcel: false,
};

export const measurement = (
  state: MeasurementState = initialState,
  action: ActionTypes
): MeasurementState => {
  switch (action.type) {
    case getType(measurementRequest):
      return {
        ...state,
        isFetching: true,
      };
    case getType(measurementSuccess):
      return {
        ...state,
        measurementResponse: (action as Action<MeasurementResponse>).payload,
        isFetching: false,
        isSuccessfullyFetched: true,
      };
    case getType(measurementFailure):
      return {
        ...state,
        error: (action as Action<Maybe<ErrorResponse>>).payload,
        isFetching: false,
        isSuccessfullyFetched: false,
      };
    case getType(exportToExcelAction):
      return {...state, isExportingToExcel: true};
    case getType(exportToExcelSuccess):
      return {...state, isExportingToExcel: false};
    case getType(measurementClearError):
    case getType(selectResolution):
    case getType(toggleComparePeriod):
    case getType(toggleShowAverage):
    case getType(setReportTimePeriod):
    case getType(addLegendItems):
    case getType(removeAllByType):
    case getType(toggleQuantityByType):
    case getType(toggleQuantityById):
    case getType(search):
      return initialState;
    default:
      return resetReducer(state, action, initialState);
  }
};
