export interface WidgetModel {
  type: Medium;
  total: number;
  pending: number;
}

export type OnSelectIndicator = (type: Medium) => void;

export const enum Medium {
  collection = 'collection',
  electricity = 'current',
  districtHeating = 'districtHeating',
  gas = 'gas',
  measurementQuality = 'measurementQuality',
  temperatureInside = 'temperatureInside',
  temperatureOutside = 'temperatureOutside',
  hotWater = 'warmWater',
  water = 'water',
  unknown = 'unknown',
}

const mediumTypes: {[key: string]: Medium} = {
  'District heating': Medium.districtHeating,
  'Gas': Medium.gas,
  'Water': Medium.water,
  'Hot water': Medium.hotWater,
  'Temperature inside': Medium.temperatureInside,
  'Electricity': Medium.electricity,

};

export const getMediumType = (key: string) => mediumTypes[key] || Medium.unknown;
