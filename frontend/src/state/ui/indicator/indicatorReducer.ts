import {EmptyAction} from 'react-redux-typescript';
import {IndicatorType} from '../../../components/indicators/indicatorWidgetModels';
import {Action} from '../../../types/Types';
import {TOGGLE_INDICATOR_WIDGET} from './indicatorActions';

export interface SelectedIndicators {
  dashboard?: IndicatorType;
  report?: IndicatorType;
}

export interface IndicatorState {
  selectedIndicators: SelectedIndicators;
}

export const initialState: IndicatorState = {selectedIndicators: {}};

type ActionTypes = EmptyAction<string> | Action<SelectedIndicators>;

export const indicator = (state: IndicatorState = initialState, action: ActionTypes): IndicatorState => {
  switch (action.type) {
    case TOGGLE_INDICATOR_WIDGET:
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
