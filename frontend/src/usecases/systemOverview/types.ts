import {WidgetModel} from '../widget/models/WidgetModels';

export interface SystemOverviewState {
  title: string,
  widgets: WidgetModel[],
}