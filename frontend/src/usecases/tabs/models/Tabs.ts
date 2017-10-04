export const TabTypes = {
  list: 'list',
  map: 'map',
  graph: 'graph',
};

interface Tabs {
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
