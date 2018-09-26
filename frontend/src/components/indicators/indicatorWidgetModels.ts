export interface WidgetModel {
  total: number;
  pending: number;
}

export type OnSelectIndicator = (type: Medium) => void;

export const enum Medium {
  electricity = 'current',
  districtHeating = 'districtHeating',
  gas = 'gas',
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
  'Electricity': Medium.electricity,
  'Room sensor': Medium.roomSensor,
};

export const getMediumType = (key: string): Medium => mediumTypes[key] || Medium.unknown;
