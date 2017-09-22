import {SystemOverviewState} from '../../systemOverview/types';

export interface DashboardModel {
  id: string | number;
  title: string;
  author: string;
  systemOverview: SystemOverviewState;
}
