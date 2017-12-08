import {Status} from '../../../types/Types';

export const enum IndicatorType {
  collection = 'collection',
  measurementQuality = 'measurementQuality',
  current = 'current',
  coldWater = 'coldWater',
  warmWater = 'warmWater',
  districtHeating = 'districtHeating',
  temperatureInside = 'temperatureInside',
  temperatureOutside = 'temperatureOutside',
}

export interface WidgetModel {
  type: IndicatorType;
  total: number;
  status: Status;
  pending: number;
}

export type OnSelectIndicator = (type: IndicatorType) => void;
