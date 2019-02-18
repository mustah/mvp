import {EmptyAction} from 'react-redux-typescript';
import {Maybe} from '../../../../helpers/Maybe';
import {Action, ErrorResponse} from '../../../../types/Types';
import {
  REMOVE_SELECTED_LIST_ITEMS,
  SELECT_RESOLUTION,
  SET_SELECTED_ITEMS
} from '../../../../usecases/report/reportActions';
import {SEARCH} from '../../../../usecases/search/searchActions';
import {resetReducer} from '../../../domain-models/domainModelsReducer';
import {
  EXPORT_TO_EXCEL,
  EXPORT_TO_EXCEL_SUCCESS,
  MEASUREMENT_CLEAR_ERROR,
  MEASUREMENT_FAILURE,
  MEASUREMENT_REQUEST,
  MEASUREMENT_SUCCESS
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
    case MEASUREMENT_REQUEST:
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
    case EXPORT_TO_EXCEL:
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
    case SELECT_RESOLUTION:
    case SET_SELECTED_ITEMS:
    case REMOVE_SELECTED_LIST_ITEMS:
    case SEARCH:
      return initialState;
    default:
      return resetReducer(state, action, initialState);
  }
};
