import {WidgetModel} from '../../components/indicators/models/widgetModels';
import {uuid} from '../../types/Types';

export interface DashboardModel {
  id: uuid;
  widgets: WidgetModel[];
}
