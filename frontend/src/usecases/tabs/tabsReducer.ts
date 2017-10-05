import {AnyAction} from 'redux';
import {TabsState, tabTypes} from './models/TabsModel';
import {TABS_CHANGE_TAB, TABS_CHANGE_TAB_OPTION} from './tabsActions';

const tabsInitialState: TabsState = {
  validation: {
    selectedTab: tabTypes.list,
    tabs: {
      [tabTypes.map]: {
        selectedOption: '',
      },
      [tabTypes.list]: {
        selectedOption: '',
      },
    },
  },
};

export const tabs = (state: TabsState = tabsInitialState, action: AnyAction) => {
  const {payload} = action;
  switch (action.type) {
    case TABS_CHANGE_TAB:
      return {
        ...state,
        [payload.useCase]: {
          ...state[payload.useCase],
          selectedTab: {...payload.tab},
        },
      };
    case TABS_CHANGE_TAB_OPTION:
      return {
        ...state,
        [payload.useCase]: {
          ...state[payload.useCase],
          tabs: {
            ...state[payload.useCase].tabs,
            [payload.tab]: {
              ...state[payload.useCase][payload.tab],
              selectedOption: {...payload.option},
            },
          },
        },
      };
    default:
      return state;
  }
};
