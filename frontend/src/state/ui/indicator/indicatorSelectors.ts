import {createSelector, OutputSelector} from 'reselect';
import {IndicatorType} from '../../../components/indicators/indicatorWidgetModels';
import {Maybe} from '../../../helpers/Maybe';
import {UseCases} from '../../../types/Types';
import {UiState} from '../uiReducer';
import {IndicatorState} from './indicatorReducer';

const getIndicatorState = (state: UiState): IndicatorState => state.indicator;

type SelectedIndicatorSelector =
  OutputSelector<UiState, IndicatorType, (state: IndicatorState) => IndicatorType>;

const getSelectedIndicator = (useCase: string): SelectedIndicatorSelector =>
  createSelector<UiState, IndicatorState, IndicatorType>(
    getIndicatorState,
    (indicator: IndicatorState) => Maybe.maybe<IndicatorType>(indicator.selectedIndicators[useCase])
      .orElse(IndicatorType.districtHeating),
  );

export const getSelectedIndicatorTypeForReport = getSelectedIndicator(UseCases.report);
