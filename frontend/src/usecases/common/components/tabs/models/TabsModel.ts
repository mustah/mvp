export enum tabType {
  list = 'list',
  map = 'map',
  graph = 'graph',
}

export interface ListProps {
  data: any;
}

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
  selectedTab: tabType;
  changeTab: (payload: {useCase: string; tab: tabType; }) => any;
  changeTabOption: (payload: {useCase: string; tab: tabType; option: string; }) => any;
}
