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

export interface TabModel {
  [key: string]: {
    selectedOption: string;
  };
}

export interface SelectedTabs {
  selectedTab: TabName;
  tabs: TabModel;
}

export interface TabsState {
  [key: string]: SelectedTabs;
}

export interface TabSelection {
  tab: TabName;
  useCase: string;
  option?: string;
}

export interface TabsContainerStateToProps {
  tabs: TabModel;
  selectedTab: TabName;
}

export interface TabsContainerDispatchToProps {
  changeTab: (tab: TabName) => void;
  changeTabOption: (tab: TabName, option: string) => void;
}
