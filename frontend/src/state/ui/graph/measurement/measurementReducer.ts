import {EmptyAction} from 'react-redux-typescript';
import {Maybe} from '../../../../helpers/Maybe';
import {Action, ErrorResponse} from '../../../../types/Types';
import {SELECT_RESOLUTION, SET_SELECTED_ENTRIES} from '../../../../usecases/report/reportActions';
import {SEARCH} from '../../../../usecases/search/searchActions';
import {resetReducer} from '../../../domain-models/domainModelsReducer';
import {
  MEASUREMENT_CLEAR_ERROR,
  MEASUREMENT_FAILURE,
  MEASUREMENT_REQUEST,
  MEASUREMENT_SUCCESS
} from './measurementActions';
import {MeasurementResponses, MeasurementState} from './measurementModels';

export type ActionTypes = | EmptyAction<string> | Action<MeasurementResponses | Maybe<ErrorResponse>>;

export const initialState: MeasurementState = {
  isFetching: false,
  isSuccessfullyFetched: false,
  error: Maybe.nothing(),
  measurementResponse: {
    measurements: [],
    average: [],
    cities: [],
  },
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
        measurementResponse: (action as Action<MeasurementResponses>).payload,
        isFetching: false,
        isSuccessfullyFetched: true,
      };
    case MEASUREMENT_FAILURE:
      return {
        ...state,
        error: (action as Action<Maybe<ErrorResponse>>).payload,
        isFetching: false,
        isSuccessfullyFetched: false
      };
    case MEASUREMENT_CLEAR_ERROR:
    case SELECT_RESOLUTION:
    case SET_SELECTED_ENTRIES:
    case SEARCH:
      return initialState;
    default:
      return resetReducer(state, action, initialState);
  }
};
