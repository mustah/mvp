import {Indicator} from '../../common/components/indicators/models/IndicatorModels';

export interface SystemOverviewState {
  title: string;
  indicators: Indicator[];
}

export interface DashboardModel {
  id: string | number;
  title: string;
  author: string;
  systemOverview: SystemOverviewState;
}
