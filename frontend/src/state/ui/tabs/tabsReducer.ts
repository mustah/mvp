import {Action} from '../../../types/Types';
import {CHANGE_TAB} from './tabsActions';
import {SelectedTabs, TabName, TabSelection, TabsState} from './tabsModels';

const overviewTab: SelectedTabs = {
  selectedTab: TabName.overview,
};

const initialState: TabsState = {
  validation: {...overviewTab},
  collection: {...overviewTab},
};

type ActionType = Action<TabSelection>;

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
    case CHANGE_TAB:
      return changeTab(state, action);
    default:
      return state;
  }
};
