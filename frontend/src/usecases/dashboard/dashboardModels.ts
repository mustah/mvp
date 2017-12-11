import {WidgetModel} from '../../components/indicators/indicatorWidgetModels';
import {uuid} from '../../types/Types';

export interface DashboardModel {
  id: uuid;
  widgets: WidgetModel[];
}
