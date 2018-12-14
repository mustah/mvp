import {Overwrite} from 'react-redux-typescript';
import {Maybe} from '../../../../helpers/Maybe';
import {ErrorResponse, Identifiable, UnixTimestamp} from '../../../../types/Types';
import {NormalizedPaginated} from '../../../domain-models-paginated/paginatedDomainModels';
import {TabName} from '../../tabs/tabsModels';

export interface Measurement extends Identifiable {
  created: UnixTimestamp;
  value?: number;
  quantity: Quantity;
  unit: string;
}

export interface MeasurementState {
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
  selectedTab: TabName;
  measurementResponse: MeasurementResponses;
}

export type MeasurementsByQuantity = Partial<{ [key in Quantity]: Measurement }>;

export interface Reading {
  id: UnixTimestamp;
  measurements: MeasurementsByQuantity;
}

export interface ExistingReadings {
  [key: number]: Reading;
}

export interface Readings {
  [key: number]: Overwrite<Reading, {measurements?: MeasurementsByQuantity}>;
}

const emptyMeasurementResponse: MeasurementResponses = {
  measurements: [],
  average: [],
  cities: [],
};

export const initialState: MeasurementState = {
  isFetching: false,
  error: Maybe.nothing(),
  selectedTab: TabName.graph,
  measurementResponse: emptyMeasurementResponse,
};

type MeasurementValues = Array<{
  when: number;
  value?: number;
}>;

export interface MeasurementResponsePart {
  id: string;
  quantity: Quantity;
  unit: string;
  label: string;
  city: string;
  address: string;
  medium: string;
  values: MeasurementValues;
}

export interface AverageResponsePart {
  id: string;
  quantity: Quantity;
  unit: string;
  label: string;
  values: MeasurementValues;
}

export type MeasurementApiResponse = MeasurementResponsePart[];

export interface Measurements {
  measurements: MeasurementApiResponse;
}

type AverageApiResponse = AverageResponsePart[];

export interface MeasurementResponses extends Measurements {
  average: AverageApiResponse;
  cities: AverageApiResponse;
}

export enum Quantity {
  volume = 'Volume',
  flow = 'Flow',
  energy = 'Energy',
  power = 'Power',
  forwardTemperature = 'Forward temperature',
  returnTemperature = 'Return temperature',
  differenceTemperature = 'Difference temperature',
  temperature = 'Temperature',
  relativeHumidity = 'Relative humidity',
  energyReturn = 'Energy return',
  energyReactive = 'Reactive energy',
  externalTemperature = 'External temperature',
}

export enum QunantityDisplayMode {
  meterValue = 1,
  consumption
}

export const quantityUnits: { [q in Quantity]: string } = {
  [Quantity.energy]: 'kWh',
  [Quantity.energyReturn]: 'kWh',
  [Quantity.energyReactive]: 'kWh',
  [Quantity.externalTemperature]: '°C',
  [Quantity.volume]: 'm³',
  [Quantity.power]: 'W',
  [Quantity.flow]: 'm³/h',
  [Quantity.forwardTemperature]: '°C',
  [Quantity.returnTemperature]: '°C',
  [Quantity.temperature]: '°C',
  [Quantity.relativeHumidity]: '%',
  [Quantity.differenceTemperature]: 'K',
};

export const quantityDisplayModes: {[q in Quantity]: QunantityDisplayMode } = {
  [Quantity.energy]: QunantityDisplayMode.consumption,
  [Quantity.energyReturn]: QunantityDisplayMode.consumption,
  [Quantity.energyReactive]: QunantityDisplayMode.consumption,
  [Quantity.externalTemperature]: QunantityDisplayMode.meterValue,
  [Quantity.volume]: QunantityDisplayMode.consumption,
  [Quantity.power]: QunantityDisplayMode.meterValue,
  [Quantity.flow]: QunantityDisplayMode.meterValue,
  [Quantity.forwardTemperature]: QunantityDisplayMode.meterValue,
  [Quantity.returnTemperature]: QunantityDisplayMode.meterValue,
  [Quantity.temperature]: QunantityDisplayMode.meterValue,
  [Quantity.relativeHumidity]: QunantityDisplayMode.meterValue,
  [Quantity.differenceTemperature]: QunantityDisplayMode.meterValue,
};

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

export const allQuantities: { [m in Medium]: Quantity[] } = {
  [Medium.districtHeating]: [
    Quantity.energy,
    Quantity.volume,
    Quantity.power,
    Quantity.flow,
    Quantity.forwardTemperature,
    Quantity.returnTemperature,
    Quantity.differenceTemperature,
  ],
  [Medium.gas]: [
    Quantity.volume,
  ],
  [Medium.water]: [
    Quantity.volume,
  ],
  [Medium.hotWater]: [
    Quantity.volume,
  ],
  [Medium.electricity]: [
    Quantity.energy,
    Quantity.energyReactive,
    Quantity.energyReturn,
    Quantity.power,
  ],
  [Medium.roomSensor]: [
    Quantity.externalTemperature,
    Quantity.relativeHumidity,
  ],
  [Medium.unknown]: [],
};

export const defaultQuantityForMedium = (medium: Medium): Quantity => allQuantities[medium][0];

export interface MeterMeasurementsState {
  isFetching: boolean;
  page: number;
  error: Maybe<ErrorResponse>;
  measurementPages: NormalizedPaginated<Measurement>;
}

export const initialMeterMeasurementsState: MeterMeasurementsState = {
  isFetching: false,
  page: 0,
  error: Maybe.nothing(),
  measurementPages: {
    page: 0,
    result: {
      content: [],
      first: true,
      last: false,
      number: 0,
      numberOfElements: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
      sort: null,
    },
    entities: {},
  },
};
