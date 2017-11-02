import {UiState} from '../uiReducer';
import {IndicatorState} from './indicatorReducer';
import {IndicatorType} from '../../../usecases/common/components/indicators/models/IndicatorModels';
import {createSelector} from 'reselect';

const getIndicatorState = (state: UiState): IndicatorState => state.indicator;

const getSelectedIndicator = (useCase: string): any =>
  createSelector<UiState, IndicatorState, IndicatorType> (
    getIndicatorState,
    (indicator: IndicatorState) => indicator.selectedIndicators[useCase],
  );

export const getSelectedIndicatorReport = getSelectedIndicator('report');
export const getSelectedIndicatorDashboard = getSelectedIndicator('dashboard');
