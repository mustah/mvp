import {createSelector, OutputSelector} from 'reselect';
import {IndicatorType} from '../../../components/indicators/indicatorWidgetModels';
import {Maybe} from '../../../helpers/Maybe';
import {useCases} from '../../../types/constants';
import {UiState} from '../uiReducer';
import {IndicatorState} from './indicatorReducer';

const getIndicatorState = (state: UiState): IndicatorState => state.indicator;

type SelectedIndicatorSelector =
  OutputSelector<UiState, Maybe<IndicatorType>, (res: IndicatorState) => Maybe<IndicatorType>>;

const getSelectedIndicator = (useCase: string): SelectedIndicatorSelector =>
  createSelector<UiState, IndicatorState, Maybe<IndicatorType>>(
    getIndicatorState,
    (indicator: IndicatorState) => Maybe.maybe<IndicatorType>(indicator.selectedIndicators[useCase]),
  );

export const getSelectedIndicatorTypeForReport = getSelectedIndicator(useCases.report);
