export const enum Medium {
  coldWater = 'coldWater',
  collection = 'collection',
  electricity = 'current',
  districtHeating = 'districtHeating',
  gas = 'gas',
  measurementQuality = 'measurementQuality',
  temperatureInside = 'temperatureInside',
  temperatureOutside = 'temperatureOutside',
  hotWater = 'warmWater',
  water = 'water',
}

export interface WidgetModel {
  type: Medium;
  total: number;
  pending: number;
}

export type OnSelectIndicator = (type: Medium) => void;
