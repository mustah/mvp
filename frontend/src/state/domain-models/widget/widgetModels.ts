import {uuid} from '../../../types/Types';
import {SelectionInterval} from '../../user-selection/userSelectionModels';

export enum WidgetType {
  MAP = 'MAP',
  COLLECTION = 'COLLECTION',
  COUNT = 'COUNT',
}

export interface WidgetMandatory {
  id: uuid;
  dashboardId: uuid;
  type: WidgetType;
}

export interface MapWidget extends WidgetMandatory {
  type: WidgetType.MAP;
  settings: {
    selectionId?: uuid;
  };
}

export interface CollectionStatusWidget extends WidgetMandatory {
  type: WidgetType.COLLECTION;
  settings: {
    selectionId?: uuid;
    selectionInterval: SelectionInterval;
  };
}

export interface CountWidget extends WidgetMandatory {
  type: WidgetType.COUNT;
  settings: {
    selectionId?: uuid;
  };
}

export type Widget = MapWidget | CollectionStatusWidget | CountWidget;
