import {NormalizedRows} from '../../table/table/Table';

export enum tabType {
  list = 'list',
  map = 'map',
  graph = 'graph',
  dashboard = 'dashboard',
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
