import {createPayloadAction} from 'react-redux-typescript';
import {IndicatorType} from '../common/components/indicators/models/IndicatorModels';
import {SelectedIndicators} from './indicatorReducer';

export const SELECT_INDICATOR_WIDGET = 'SELECT_INDICATOR_WIDGET';

export const selectIndicatorWidget = createPayloadAction<string, SelectedIndicators>(SELECT_INDICATOR_WIDGET);

export const selectDashboardIndicatorWidget =
  (type: IndicatorType) => dispatch => dispatch(selectIndicatorWidget({dashboard: type}));

export const selectReportIndicatorWidget =
  (type: IndicatorType) => dispatch => dispatch(selectIndicatorWidget({report: type}));
