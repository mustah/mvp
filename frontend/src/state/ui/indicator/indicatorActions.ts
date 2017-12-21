import {createPayloadAction} from 'react-redux-typescript';
import {IndicatorType} from '../../../components/indicators/indicatorWidgetModels';
import {SelectedIndicators} from './indicatorReducer';

export const SELECT_INDICATOR_WIDGET = 'SELECT_INDICATOR_WIDGET';

export const selectIndicatorWidget = createPayloadAction<string, SelectedIndicators>(SELECT_INDICATOR_WIDGET);

export const selectReportIndicatorWidget =
  (type: IndicatorType) => (dispatch) => dispatch(selectIndicatorWidget({report: type}));
