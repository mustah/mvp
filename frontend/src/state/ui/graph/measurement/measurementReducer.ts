import {ActionType, getType} from 'typesafe-actions';
import {Maybe} from '../../../../helpers/Maybe';
import {resetReducer} from '../../../../reducers/resetReducer';
import {Action, ErrorResponse} from '../../../../types/Types';
import * as reportActions from '../../../report/reportActions';
import {ReportSector} from '../../../report/reportModels';
import {search} from '../../../search/searchActions';
import * as actions from './measurementActions';
import {MeasurementResponse, MeasurementState} from './measurementModels';

type ActionTypes = ActionType<typeof actions | typeof reportActions | typeof search>
  | Action<Maybe<ErrorResponse> | MeasurementResponse>;

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

const measurementReducerFor =
  (sector: ReportSector) =>
    (state: MeasurementState = initialState, action: ActionTypes): MeasurementState => {
      switch (action.type) {
        case getType(actions.measurementRequest(sector)):
          return {
            ...state,
            isFetching: true,
          };
        case getType(actions.measurementSuccess(sector)):
          return {
            ...state,
            measurementResponse: (action as Action<MeasurementResponse>).payload,
            isFetching: false,
            isSuccessfullyFetched: true,
          };
        case getType(actions.measurementFailure(sector)):
          return {
            ...state,
            error: (action as Action<Maybe<ErrorResponse>>).payload,
            isFetching: false,
            isSuccessfullyFetched: false,
          };
        case getType(actions.exportToExcelAction(sector)):
          return {...state, isExportingToExcel: true};
        case getType(actions.exportToExcelSuccess(sector)):
          return {...state, isExportingToExcel: false};
        case getType(actions.measurementClearError(sector)):
        case getType(reportActions.selectResolution(sector)):
        case getType(reportActions.toggleComparePeriod(sector)):
        case getType(reportActions.toggleShowAverage(sector)):
        case getType(reportActions.setReportTimePeriod(sector)):
        case getType(reportActions.addLegendItems(sector)):
        case getType(reportActions.removeAllByType(sector)):
        case getType(reportActions.toggleQuantityByType(sector)):
        case getType(reportActions.toggleQuantityById(sector)):
        default:
          return resetReducer(state, action, initialState);
      }
    };

export const measurement = measurementReducerFor(ReportSector.report);

export const selectionMeasurement = measurementReducerFor(ReportSector.selectionReport);
