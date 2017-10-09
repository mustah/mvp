export enum tabTypes {
  list = 'list',
  map = 'map',
  graph = 'graph',
}

export type TabIdentifier = tabTypes;

export interface TabModel {
  [key: string]: {
    selectedOption: string;
  };
}

export interface SelectedTabs {
  selectedTab: string;
  tabs: TabModel;
}

export interface TabsState {
  [key: string]: SelectedTabs;
}

export interface TabsContainerProps {
  tabs: TabModel;
  selectedTab: TabIdentifier;
  changeTab: (payload: {useCase: string; tab: TabIdentifier; }) => any;
  changeTabOption: (payload: {useCase: string; tab: TabIdentifier; option: string; }) => any;
}
