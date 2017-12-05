import {createSelector, OutputSelector} from 'reselect';
import {IndicatorType} from '../../../components/indicators/models/widgetModels';
import {UiState} from '../uiReducer';
import {IndicatorState} from './indicatorReducer';

const getIndicatorState = (state: UiState): IndicatorState => state.indicator;

type SelectedIndicatorSelector = OutputSelector<UiState, IndicatorType, (res: IndicatorState) => IndicatorType>;
const getSelectedIndicator = (useCase: string): SelectedIndicatorSelector =>
  createSelector<UiState, IndicatorState, IndicatorType>(
    getIndicatorState,
    (indicator: IndicatorState) => indicator.selectedIndicators[useCase],
  );

export const getSelectedIndicatorReport = getSelectedIndicator('report');
