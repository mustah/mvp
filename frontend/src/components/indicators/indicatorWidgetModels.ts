export interface WidgetModel {
  type: Medium;
  total: number;
  pending: number;
}

export type OnSelectIndicator = (type: Medium) => void;

export const enum Medium {
  collection = 'collection', // TODO remove collection, it is not a medium
  electricity = 'current',
  districtHeating = 'districtHeating',
  gas = 'gas',
  measurementQuality = 'measurementQuality',
  temperatureInside = 'temperatureInside',
  temperatureOutside = 'temperatureOutside',
  hotWater = 'warmWater',
  roomSensor = 'roomSensor',
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
  'Room sensor': Medium.roomSensor,
};

export const getMediumType = (key: string): Medium => mediumTypes[key] || Medium.unknown;
