export const enum tabType {
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
  selectedTab: tabType;
  tabs: TabModel;
}

export interface TabsState {
  [key: string]: SelectedTabs;
}

export interface TabsContainerStateToProps {
  tabs: TabModel;
  selectedTab: tabType;
}

export interface TabsContainerDispatchToProps {
  changeTab: (tab: tabType) => void;
  changeTabOption: (tab: tabType, option: string) => void;
}
