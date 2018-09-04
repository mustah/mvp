import {UseCases} from '../../../types/Types';

export const enum TabName {
  list = 'list',
  map = 'map',
  graph = 'graph',
  table = 'table',
  overview = 'overview',
  values = 'values',
  log = 'log',
  connectedGateways = 'connectedGateways',
}

export interface SelectedTabs {
  selectedTab: TabName;
}

export interface TabsState {
  [key: string]: SelectedTabs;
}

export interface TabSelection {
  tab: TabName;
  useCase: UseCases.collection | UseCases.validation;
}

export interface SelectedTab {
  selectedTab: TabName;
}

export interface TabsContainerDispatchToProps {
  changeTab: (tab: TabName) => void;
}
