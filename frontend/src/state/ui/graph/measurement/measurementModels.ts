import {Medium} from '../../../../components/indicators/indicatorWidgetModels';
import {Maybe} from '../../../../helpers/Maybe';
import {ErrorResponse, Identifiable, uuid} from '../../../../types/Types';
import {ReportContainerState} from '../../../../usecases/report/containers/ReportContainer';
import {NormalizedPaginated} from '../../../domain-models-paginated/paginatedDomainModels';
import {TabName} from '../../tabs/tabsModels';

export interface Measurement extends Identifiable {
  created: number;
  value: number;
  quantity: Quantity;
  unit: string;
}

export interface Reading {
  id: uuid;
  measurements: Measurement[];
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
}

// TODO type, and fill in missing values for, {[q in Quantity]: string}
export const quantityUnits = {
  [Quantity.energy]: 'kWh',
  [Quantity.volume]: 'm',
  [Quantity.power]: 'W',
  [Quantity.flow]: 'm³/h',
  [Quantity.forwardTemperature]: '°C',
  [Quantity.returnTemperature]: '°C',
  [Quantity.differenceTemperature]: 'K',
};

// TODO type and correct erronous usages
export const allQuantities = {
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
  [Medium.temperatureInside]: [
    Quantity.temperature,
    Quantity.relativeHumidity,
  ],
  [Medium.electricity]: [
    Quantity.energy,
    Quantity.energyReactive,
    Quantity.energyReturn,
    Quantity.power,
  ],
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
