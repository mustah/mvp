import {EmptyAction} from 'react-redux-typescript';
import {Medium} from '../../../components/indicators/indicatorWidgetModels';
import {Action} from '../../../types/Types';
import {IndicatorWithinUseCase, TOGGLE_INDICATOR_WIDGET} from './indicatorActions';

export interface SelectedIndicators {
  report: Medium[];
}

export interface IndicatorState {
  selectedIndicators: SelectedIndicators;
}

export const initialState: IndicatorState = {
  selectedIndicators: {
    report: [],
  },
};

type ActionTypes = EmptyAction<string> | Action<IndicatorWithinUseCase>;

export const indicator = (state: IndicatorState = initialState, action: ActionTypes): IndicatorState => {
  switch (action.type) {
    case TOGGLE_INDICATOR_WIDGET:
      const [section, indicatorTypeToToggle] = (action as Action<IndicatorWithinUseCase>).payload;
      let selectedInSection = [...state.selectedIndicators[section]];

      if (selectedInSection.includes(indicatorTypeToToggle)) {
        selectedInSection = selectedInSection.filter(
          (indicatorType) => indicatorType !== indicatorTypeToToggle,
        );
      } else {
        selectedInSection.push(indicatorTypeToToggle);
      }

      return {
        ...state,
        selectedIndicators: {
          ...state.selectedIndicators,
          [section]: selectedInSection,
        },
      };
    default:
      return state;
  }
};
