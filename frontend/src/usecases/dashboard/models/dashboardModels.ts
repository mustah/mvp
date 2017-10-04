import {DonutGraph} from '../../common/components/indicators/models/DonutGraphModels';
import {Indicator} from '../../common/components/indicators/models/IndicatorModels';

export interface SystemOverviewState {
  title: string;
  indicators: Indicator[];
  donutGraphs: DonutGraph[];
}

export interface DashboardModel {
  id: string | number;
  title: string;
  author: string;
  systemOverview: SystemOverviewState;
}
