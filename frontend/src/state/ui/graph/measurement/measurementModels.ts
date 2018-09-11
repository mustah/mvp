import {Medium} from '../../../../components/indicators/indicatorWidgetModels';
import {Maybe} from '../../../../helpers/Maybe';
import {Identifiable} from '../../../../types/Types';
import {ReportContainerState} from '../../../../usecases/report/containers/ReportContainer';
import {TabName} from '../../tabs/tabsModels';

export interface Measurement extends Identifiable {
  created: number;
  value: number;
  quantity: Quantity;
  unit: string;
}

const emptyMeasurementResponse: MeasurementResponses = {
  measurement: [],
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

export interface MeasurementApiResponsePart {
  id: string;
  quantity: Quantity;
  unit: string;
  label: string;
  city: string;
  address: string;
  medium: string;
  values: Array<{
    when: number;
    value?: number;
  }>;
}

export type MeasurementApiResponse = MeasurementApiResponsePart[];

export interface MeasurementResponses {
  measurement: MeasurementApiResponse;
  average: MeasurementApiResponse;
  cities: MeasurementApiResponse;
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

export const quantityUnits = {
  [Quantity.energy]: 'kWh',
  [Quantity.volume]: 'm',
  [Quantity.power]: 'W',
  [Quantity.flow]: 'm³/h',
  [Quantity.forwardTemperature]: '°C',
  [Quantity.returnTemperature]: '°C',
  [Quantity.differenceTemperature]: 'K',
};

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
