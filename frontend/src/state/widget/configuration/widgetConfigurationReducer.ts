import {uuid} from '../../../types/Types';
import {CollectionStatusWidgetSettings} from '../../../usecases/dashboard/containers/CollectionStatusContainer';
import {CountWidgetSettings} from '../../../usecases/dashboard/containers/CountWidgetContainer';
import {MapWidgetSettings} from '../../../usecases/dashboard/containers/MapWidgetContainer';

export enum WidgetType {
  MAP = 'MAP',
  COLLECTION = 'COLLECTION',
  COUNT = 'COUNT',
}

export const widgetSizeMap: {[w in WidgetType]: LayoutProps} = {
  [WidgetType.MAP]: {w: 5, h: 4},
  [WidgetType.COLLECTION]: {w: 1, h: 1},
  [WidgetType.COUNT]: {w: 1, h: 1},
};

export interface LayoutProps {
  w: number;
  h: number;
}

export const widgetMargins: [number, number] = [24, 24];

export const widgetHeighToPx = (height: number): number =>
  height * 170 + (24 * (height - 1)) - 52;

export const widgetWidthToPx = (width: number): number =>
  width * 172 + (24 * (width - 1));

export interface WidgetMandatory {
  id: uuid;
  dashboardId: uuid;
  type: WidgetType;
}

export type WidgetSettings = MapWidgetSettings | CollectionStatusWidgetSettings | CountWidgetSettings;
