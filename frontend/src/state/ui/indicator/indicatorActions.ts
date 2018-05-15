import {createPayloadAction} from 'react-redux-typescript';
import {Medium} from '../../../components/indicators/indicatorWidgetModels';
import {SelectedIndicators} from './indicatorReducer';

export const TOGGLE_INDICATOR_WIDGET = 'TOGGLE_INDICATOR_WIDGET';

export type IndicatorWithinUseCase = [keyof SelectedIndicators, Medium];

export const toggleIndicatorWidget = createPayloadAction<string, IndicatorWithinUseCase>(TOGGLE_INDICATOR_WIDGET);

export const toggleReportIndicatorWidget =
  (type: Medium) =>
    (dispatch) => dispatch(toggleIndicatorWidget(['report', type]));
