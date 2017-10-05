export const TabTypes = {
  list: 'list',
  map: 'map',
  graph: 'graph',
};

export interface Tab {
  [key: string]: {
    selectedOption: string | null;
  };
}

export interface TabView {
  selectedTab: string;
  tabs: Tab;
}

export interface TabsState {
  [key: string]: TabView;
}
