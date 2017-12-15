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
  useCase: string;
}

export interface TabsContainerStateToProps {
  selectedTab: TabName;
}

export interface TabsContainerDispatchToProps {
  changeTab: (tab: TabName) => void;
}
