import {Maybe} from '../../../../helpers/Maybe';
import {ErrorResponse, Identifiable, uuid} from '../../../../types/Types';
import {ReportContainerState} from '../../../../usecases/report/containers/ReportContainer';
import {NormalizedPaginated} from '../../../domain-models-paginated/paginatedDomainModels';
import {TabName} from '../../tabs/tabsModels';

export interface Measurement extends Identifiable {
  created: number;
  value?: number;
  quantity: Quantity;
  unit: string;
}

export type MeasurementsByQuantity = Partial<{[key in Quantity]: Measurement}>;

export interface Reading {
  id: uuid;
  measurements: MeasurementsByQuantity;
}

const emptyMeasurementResponse: MeasurementResponses = {
  measurements: [],
  average: [],
  cities: [],
};

export const initialState: ReportContainerState = {
  hiddenKeys: [],
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

export interface CityResponsePart {
  id: string;
  quantity: Quantity;
  unit: string;
  label: string;
  city: string;
  values: MeasurementValues;
}

export type MeasurementApiResponse = MeasurementResponsePart[];
export type AverageApiResponse = AverageResponsePart[];
export type CityApiResponse = CityResponsePart[];

export interface Measurements {
  measurements: MeasurementApiResponse;
}

export interface MeasurementResponses extends Measurements {
  average: AverageApiResponse;
  cities: CityApiResponse;
}

export const enum Quantity {
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

export const quantityUnits: {[q in Quantity]: string} = {
  [Quantity.energy]: 'kWh',
  [Quantity.energyReturn]: 'kWh',
  [Quantity.energyReactive]: 'kWh',
  [Quantity.externalTemperature]: '°C',
  [Quantity.volume]: 'm',
  [Quantity.power]: 'W',
  [Quantity.flow]: 'm³/h',
  [Quantity.forwardTemperature]: '°C',
  [Quantity.returnTemperature]: '°C',
  [Quantity.temperature]: '°C',
  [Quantity.relativeHumidity]: '%',
  [Quantity.differenceTemperature]: 'K',
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

export const allQuantities: {[m in Medium]: Quantity[]} = {
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
