import {createPayloadAction} from 'react-redux-typescript';
import {SelectedIndicators} from './indicatorReducer';
import {IndicatorType} from '../../../components/indicators/models/widgetModels';

export const SELECT_INDICATOR_WIDGET = 'SELECT_INDICATOR_WIDGET';

export const selectIndicatorWidget = createPayloadAction<string, SelectedIndicators>(SELECT_INDICATOR_WIDGET);

export const selectDashboardIndicatorWidget =
  (type: IndicatorType) => dispatch => dispatch(selectIndicatorWidget({dashboard: type}));

export const selectReportIndicatorWidget =
  (type: IndicatorType) => dispatch => dispatch(selectIndicatorWidget({report: type}));
