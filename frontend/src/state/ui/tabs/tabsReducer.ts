import {ActionType, getType} from 'typesafe-actions';
import {Action} from '../../../types/Types';
import {changeTab as changeTabAction, unknownAction} from './tabsActions';
import {TabName, TabSelection, TabsState} from './tabsModels';

export const initialState: TabsState = {
  validation: {selectedTab: TabName.list},
  collection: {selectedTab: TabName.list},
  report: {selectedTab: TabName.graph},
};

type ActionTypes = ActionType<typeof changeTabAction | typeof unknownAction>;

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

export const tabs = (state: TabsState = initialState, action: ActionTypes): TabsState => {
  if (action.type === getType(changeTabAction)) {
    return changeTab(state, action);
  } else {
    return state;
  }
};
