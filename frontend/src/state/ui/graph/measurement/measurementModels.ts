import {Overwrite} from 'react-redux-typescript';
import {Maybe} from '../../../../helpers/Maybe';
import {ErrorResponse, Identifiable, UnixTimestamp} from '../../../../types/Types';
import {NormalizedPaginated} from '../../../domain-models-paginated/paginatedDomainModels';
import {MeasurementParameters} from './measurementActions';

export interface Measurement extends Identifiable {
  created: UnixTimestamp;
  value?: number;
  quantity: Quantity;
  unit: string;
}

export interface MeasurementState {
  isFetching: boolean;
  isSuccessfullyFetched: boolean;
  error: Maybe<ErrorResponse>;
  measurementResponse: MeasurementResponses;
  isExportingToExcel: boolean;
}

export type MeasurementsByQuantity = Partial<{ [key in Quantity]: Measurement }>;
export type FetchMeasurements = (requestParameters: MeasurementParameters) => void;

export interface Reading {
  id: UnixTimestamp;
  measurements: MeasurementsByQuantity;
}

export interface ExistingReadings {
  [key: number]: Reading;
}

export type PossibleReading = Overwrite<Reading, {measurements?: MeasurementsByQuantity}>;

export interface Readings {
  [key: number]: PossibleReading;
}

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

export interface QuantityAttributes {
  unit: string;
  displayMode: QuantityDisplayMode;
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

enum QuantityDisplayMode {
  meterValue = 1,
  consumption
}

export const unitPerHour = (quantity: string | undefined, unit: string | undefined): string | undefined => {
  if (!quantity || !unit) {
    return unit;
  }
  return quantityAttributes[quantity as Quantity].displayMode === QuantityDisplayMode.consumption
    ? `${unit}/h`
    : unit;
};

export const quantityAttributes: { [q in Quantity]: QuantityAttributes } = {
  [Quantity.energy]: {unit: 'kWh', displayMode: QuantityDisplayMode.consumption},
  [Quantity.energyReturn]: {unit: 'kWh', displayMode: QuantityDisplayMode.consumption},
  [Quantity.energyReactive]: {unit: 'kWh', displayMode: QuantityDisplayMode.consumption},
  [Quantity.externalTemperature]: {unit: '°C', displayMode: QuantityDisplayMode.meterValue},
  [Quantity.volume]: {unit: 'm³', displayMode: QuantityDisplayMode.consumption},
  [Quantity.power]: {unit: 'W', displayMode: QuantityDisplayMode.meterValue},
  [Quantity.flow]: {unit: 'm³/h', displayMode: QuantityDisplayMode.meterValue},
  [Quantity.forwardTemperature]: {unit: '°C', displayMode: QuantityDisplayMode.meterValue},
  [Quantity.returnTemperature]: {unit: '°C', displayMode: QuantityDisplayMode.meterValue},
  [Quantity.temperature]: {unit: '°C', displayMode: QuantityDisplayMode.meterValue},
  [Quantity.relativeHumidity]: {unit: '%', displayMode: QuantityDisplayMode.meterValue},
  [Quantity.differenceTemperature]: {unit: 'K', displayMode: QuantityDisplayMode.meterValue},
};

export const getDisplayModeText = (quantity: Quantity | string | undefined): string => {
  const quantityAttribute = quantityAttributes[quantity as Quantity];
  return quantityAttribute && quantityAttribute.displayMode === QuantityDisplayMode.consumption
    ? 'consumption'
    : 'meter value';
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

const mediumTexts: { [medium in Medium]: string } = {
  [Medium.districtHeating]: 'District heating',
  [Medium.gas]: 'Gas',
  [Medium.water]: 'Water',
  [Medium.hotWater]: 'Hot water',
  [Medium.electricity]: 'Electricity',
  [Medium.roomSensor]: 'Room sensor',
  [Medium.unknown]: 'Unknown',
};

export const toMediumText = (medium: Medium): string => mediumTexts[medium] || mediumTexts[Medium.unknown];

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
