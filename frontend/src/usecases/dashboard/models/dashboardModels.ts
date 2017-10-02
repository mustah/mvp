import {WidgetModel} from '../../widget/models/WidgetModel';

export interface SystemOverviewState {
  title: string;
  widgets: WidgetModel[];
}

export interface DashboardModel {
  id: string | number;
  title: string;
  author: string;
  systemOverview: SystemOverviewState;
}
