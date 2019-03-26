import {ActionType, getType} from 'typesafe-actions';
import {Maybe} from '../../../../helpers/Maybe';
import {resetReducer} from '../../../../reducers/resetReducer';
import * as reportActions from '../../../../usecases/report/reportActions';
import {search} from '../../../search/searchActions';
import * as actions from './measurementActions';
import {MeasurementState} from './measurementModels';

type ActionTypes = ActionType<typeof actions | typeof reportActions | typeof search>;

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

export const measurement = (state: MeasurementState = initialState, action: ActionTypes): MeasurementState => {
  switch (action.type) {
    case getType(actions.measurementRequest):
      return {
        ...state,
        isFetching: true,
      };
    case getType(actions.measurementSuccess):
      return {
        ...state,
        measurementResponse: action.payload,
        isFetching: false,
        isSuccessfullyFetched: true,
      };
    case getType(actions.measurementFailure):
      return {
        ...state,
        error: action.payload,
        isFetching: false,
        isSuccessfullyFetched: false,
      };
    case getType(actions.exportToExcelAction):
      return {...state, isExportingToExcel: true};
    case getType(actions.exportToExcelSuccess):
      return {...state, isExportingToExcel: false};
    case getType(actions.measurementClearError):
    case getType(reportActions.selectResolution):
    case getType(reportActions.toggleComparePeriod):
    case getType(reportActions.toggleShowAverage):
    case getType(reportActions.setReportTimePeriod):
    case getType(reportActions.addLegendItems):
    case getType(reportActions.removeAllByType):
    case getType(reportActions.toggleQuantityByType):
    case getType(reportActions.toggleQuantityById):
    case getType(search):
      return initialState;
    default:
      return resetReducer(state, action, initialState);
  }
};
