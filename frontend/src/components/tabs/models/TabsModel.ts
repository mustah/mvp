export const enum TopLevelTab {
  list = 'list',
  map = 'map',
  graph = 'graph',
  table = 'table',
  overview = 'overview',
  values = 'values',
  log = 'log',
  connectedGateways = 'connectedGateways',
}

export interface TabModel {
  [key: string]: {
    selectedOption: string;
  };
}

export interface SelectedTabs {
  selectedTab: TopLevelTab;
  tabs: TabModel;
}

export interface TabsState {
  [key: string]: SelectedTabs;
}

export interface TabsContainerStateToProps {
  tabs: TabModel;
  selectedTab: TopLevelTab;
}

export interface TabsContainerDispatchToProps {
  changeTab: (tab: TopLevelTab) => void;
  changeTabOption: (tab: TopLevelTab, option: string) => void;
}
