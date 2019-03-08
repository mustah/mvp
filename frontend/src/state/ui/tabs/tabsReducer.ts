import {getType} from 'typesafe-actions';
import {EmptyAction} from 'typesafe-actions/dist/types';
import {Action} from '../../../types/Types';
import {changeTab as changeTabAction} from './tabsActions';
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
    case getType(changeTabAction):
      return changeTab(state, (action as Action<TabSelection>));
    default:
      return state;
  }
};
