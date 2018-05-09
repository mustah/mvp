import {Status} from '../../types/Types';

export const enum Medium {
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
  type: Medium;
  total: number;
  status: Status;
  pending: number;
}

export type OnSelectIndicator = (type: Medium) => void;
