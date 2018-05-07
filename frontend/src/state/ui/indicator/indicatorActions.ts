import {createPayloadAction} from 'react-redux-typescript';
import {IndicatorType} from '../../../components/indicators/indicatorWidgetModels';
import {SelectedIndicators} from './indicatorReducer';

export const TOGGLE_INDICATOR_WIDGET = 'TOGGLE_INDICATOR_WIDGET';

export const toggleIndicatorWidget = createPayloadAction<string, SelectedIndicators>(TOGGLE_INDICATOR_WIDGET);

export const toggleReportIndicatorWidget =
  (type: IndicatorType) => (dispatch) => dispatch(toggleIndicatorWidget({report: type}));
