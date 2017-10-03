import {AnyAction} from 'redux';
import {CHANGE_TAB} from './viewSwitchActions';

export interface TabView {
  selectedTab: string;
}

export interface TabsState {
  [key: string]: TabView;
}

const tabsInitialState: TabsState = {
  validation: {selectedTab: 'list'},
  dashboard: {selectedTab: 'map'},
};

export const tabs = (state: TabsState = tabsInitialState, action: AnyAction) => {
  const {payload} = action;
  switch (action.type) {
    case CHANGE_TAB:
      return {
        ...state,
        [payload.useCase]: {selectedTab: payload.tab},
      };
    default:
      return state;
  }
};
