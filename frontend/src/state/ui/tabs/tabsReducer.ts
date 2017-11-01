import {AnyAction} from 'redux';
import {TabsState, tabType} from '../../../usecases/common/components/tabs/models/TabsModel';
import {TABS_CHANGE_TAB, TABS_CHANGE_TAB_OPTION} from './tabsActions';

const tabsInitialState: TabsState = {
  validation: {
    selectedTab: tabType.list,
    tabs: {
      [tabType.map]: {
        selectedOption: 'area',
      },
    },
  },
  dashboard: {
    selectedTab: tabType.map,
    tabs: {
      [tabType.map]: {
        selectedOption: 'area',
      },
    },
  },
  collection: {
    selectedTab: tabType.list,
    tabs: {
      [tabType.graph]: {
        selectedOption: 'population',
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
          selectedTab: payload.tab,
        },
      };
    case TABS_CHANGE_TAB_OPTION:
      const {payload: {useCase, tab, option}} = action;
      return {
        ...state,
        [useCase]: {
          ...state[useCase],
          tabs: {
            ...state[useCase].tabs,
            [tab]: {
              ...state[useCase][tab],
              selectedOption: option,
            },
          },
        },
      };
    default:
      return state;
  }
};
