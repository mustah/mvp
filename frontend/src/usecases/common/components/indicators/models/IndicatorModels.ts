import {State} from '../../../../../types/Types';

export enum IndicatorType {
  collection = 'collection',
  measurementQuality = 'measurementQuality',
  current = 'current',
  coldWater = 'coldWater',
  warmWater = 'warmWater',
  districtHeating = 'districtHeating',
  temperatureInside = 'temperatureInside',
  temperatureOutside = 'temperatureOutside',
}

export interface Indicator {
  type: IndicatorType;
  title: string;
  state: State;
  subtitle: string;
  value: string;
  // quantity?: string; // Quantity is something measured, like "energy", "water"
  unit: string; // Unit is what we are measuring the value in, like "kWh", "m^3"
}
