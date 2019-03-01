import {UseCases} from '../../../types/Types';

export const enum TabName {
  list = 'list',
  map = 'map',
  graph = 'graph',
  table = 'table',
  values = 'values',
  collection = 'collection',
  log = 'log',
  connectedGateways = 'connectedGateways',
}

type TabUseCases = UseCases.collection | UseCases.validation | UseCases.report;

export interface SelectedTabs {
  selectedTab: TabName;
}

export type TabsState = {
  [key in TabUseCases]: SelectedTabs;
};

export interface TabSelection {
  tab: TabName;
  useCase: TabUseCases;
}

export interface SelectedTab {
  selectedTab: TabName;
}
