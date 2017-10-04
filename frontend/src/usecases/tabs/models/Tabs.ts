export const TabTypes = {
  list: 'list',
  map: 'map',
  graph: 'graph',
};

export interface Tabs {
  [key: string]: {
    selectedOption: string | null;
  };
}

export interface TabView {
  selectedTab: string;
  tabs: Tabs;
}

export interface TabsState {
  [key: string]: TabView;
}
