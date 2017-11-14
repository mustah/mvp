import {uuid} from '../../../types/Types';
import {WidgetModel} from '../../common/components/indicators/models/widgetModels';

export interface SystemOverviewState {
  widgets: WidgetModel[];
}

export interface DashboardModel {
  id: uuid;
  systemOverview: SystemOverviewState;
}
