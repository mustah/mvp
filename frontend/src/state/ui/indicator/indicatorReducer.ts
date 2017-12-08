import {EmptyAction} from 'react-redux-typescript';
import {IndicatorType} from '../../../components/indicators/indicatorWidgetModels';
import {Action, Maybe} from '../../../types/Types';
import {SELECT_INDICATOR_WIDGET} from './indicatorActions';

export interface SelectedIndicators {
  dashboard?: Maybe<IndicatorType>;
  report?: Maybe<IndicatorType>;
}

export interface IndicatorState {
  selectedIndicators: SelectedIndicators;
}

export const initialState = {
  selectedIndicators: {
    dashboard: null,
    report: null,
  },
};

type ActionTypes = EmptyAction<string> | Action<SelectedIndicators>;

export const indicator = (state: IndicatorState = initialState, action: ActionTypes): IndicatorState => {
  switch (action.type) {
    case SELECT_INDICATOR_WIDGET:
      return {
        ...state,
        selectedIndicators: {
          ...state.selectedIndicators,
          ...(action as Action<SelectedIndicators>).payload,
        },
      };
    default:
      return state;
  }
};
