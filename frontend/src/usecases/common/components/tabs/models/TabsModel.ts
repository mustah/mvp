import {NormalizedRows} from '../../table/Table';

export enum tabType {
  list = 'list',
  map = 'map',
  graph = 'graph',
  table = 'table',
  overview = 'overview',
  values = 'values',
  log = 'log',
  connectedGateways = 'connectedGateways',
}

export interface ListProps {
  data: NormalizedRows;
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

export interface TabsContainerProps {
  tabs: TabModel;
  selectedTab: tabType;
  changeTab: (tab: tabType) => void;
  changeTabOption: (tab: tabType, option: string) => void;
}
