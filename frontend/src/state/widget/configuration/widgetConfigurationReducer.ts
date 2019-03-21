import {uuid} from '../../../types/Types';
import {CollectionStatusWidgetSettings} from '../../../usecases/dashboard/containers/CollectionStatusContainer';
import {MapWidgetSettings} from '../../../usecases/dashboard/containers/MapWidgetContainer';

export enum WidgetType {
  MAP = 'MAP',
  COLLECTION = 'COLLECTION',
}

export const widgetSizeMap: {[w in WidgetType]: LayoutProps} = {
  [WidgetType.MAP]: {w: 5, h: 4},
  [WidgetType.COLLECTION]: {w: 1, h: 1},
};

export interface LayoutProps {
  w: number;
  h: number;
}

export interface WidgetMandatory {
  id: uuid;
  dashboardId: uuid;
  type: WidgetType;
}

export type WidgetSettings = MapWidgetSettings | CollectionStatusWidgetSettings;
