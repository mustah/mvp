import {EmptyAction} from 'react-redux-typescript';
import {Action} from '../../../types/Types';
import {CHANGE_TAB} from './tabsActions';
import {TabName, TabSelection, TabsState} from './tabsModels';

export const initialState: TabsState = {
  validation: {selectedTab: TabName.list},
  collection: {selectedTab: TabName.list},
  report: {selectedTab: TabName.graph},
};

type ActionType = Action<TabSelection> | EmptyAction<string>;

const changeTab = (state: TabsState = initialState, action: Action<TabSelection>): TabsState => {
  const {payload: {useCase, tab: selectedTab}} = action;
  return {
    ...state,
    [useCase]: {
      ...state[useCase],
      selectedTab,
    },
  };
};

export const tabs = (state: TabsState = initialState, action: ActionType): TabsState => {
  switch (action.type) {
    case CHANGE_TAB:
      return changeTab(state, (action as Action<TabSelection>));
    default:
      return state;
  }
};
