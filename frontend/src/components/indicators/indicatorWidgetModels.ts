import {Status} from '../../types/Types';

export const enum IndicatorType {
  coldWater = 'coldWater',
  collection = 'collection',
  current = 'current',
  districtHeating = 'districtHeating',
  gas = 'gas',
  measurementQuality = 'measurementQuality',
  temperatureInside = 'temperatureInside',
  temperatureOutside = 'temperatureOutside',
  warmWater = 'warmWater',
}

export interface WidgetModel {
  type: IndicatorType;
  total: number;
  status: Status;
  pending: number;
}

export type OnSelectIndicator = (type: IndicatorType) => void;
