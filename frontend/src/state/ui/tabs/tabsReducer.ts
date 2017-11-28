import {Action} from '../../../types/Types';
import {TABS_CHANGE_TAB, TABS_CHANGE_TAB_OPTION} from './tabsActions';
import {SelectedTabs, TabSelection, TabsState, TabName} from './tabsModels';

const overviewTab: SelectedTabs = {
  selectedTab: TabName.overview,
  tabs: {
    [TabName.overview]: {
      selectedOption: 'all',
    },
  },
};

const initialState: TabsState = {
  validation: {...overviewTab},
  collection: {...overviewTab},
};

type ActionType = Action<TabSelection>;

const changeTabOption = (state: TabsState = initialState, action: Action<TabSelection>): TabsState => {
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
};

const changeTab = (state: TabsState = initialState, action: Action<TabSelection>): TabsState => {
  const {payload: {useCase, tab}} = action;
  return {
    ...state,
    [useCase]: {
      ...state[useCase],
      selectedTab: tab,
    },
  };
};

export const tabs = (state: TabsState = initialState, action: ActionType): TabsState => {
  switch (action.type) {
    case TABS_CHANGE_TAB:
      return changeTab(state, action);
    case TABS_CHANGE_TAB_OPTION:
      return changeTabOption(state, action);
    default:
      return state;
  }
};
