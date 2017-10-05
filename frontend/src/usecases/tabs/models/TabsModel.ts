export const tabTypes = {
  list: 'list',
  map: 'map',
  graph: 'graph',
};

export interface Tab {
  [key: string]: {
    selectedOption: string;
  };
}

export interface TabView {
  selectedTab: string;
  tabs: Tab;
}

export interface TabsState {
  [key: string]: TabView;
}
