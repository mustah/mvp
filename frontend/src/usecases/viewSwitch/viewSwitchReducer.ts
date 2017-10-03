import {AnyAction} from 'redux';
import {VIEW_SWITCH_CHANGE_TAB} from './viewSwitchActions';

export interface TabView {
  selectedTab: string;
}

export interface ViewSwitchState {
  [key: string]: TabView;
}

const viewSwitchInitialState: ViewSwitchState = {
  validation: {selectedTab: 'list'},
  dashboard: {selectedTab: 'map'},
};

export const viewSwitch = (state: ViewSwitchState = viewSwitchInitialState, action: AnyAction) => {
  const {payload} = action;
  switch (action.type) {
    case VIEW_SWITCH_CHANGE_TAB:
      return {
        ...state,
        [payload.useCase]: {selectedTab: payload.tab},
      };
    default:
      return state;
  }
};
