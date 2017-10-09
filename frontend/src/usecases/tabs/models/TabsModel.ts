export const tabTypes = {
  list: 'list',
  map: 'map',
  graph: 'graph',
};

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
  selectedTab: string;
  changeTab: (payload: {useCase: string; tab: string; }) => any;
  changeTabOption: (payload: {useCase: string; tab: string; option: string; }) => any;
}
